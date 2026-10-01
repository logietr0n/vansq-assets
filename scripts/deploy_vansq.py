"""Copy vansq into the Modrinth Vanilla^2 profile without breaking Theseus 0.21+ content hashes.

Modrinth now stores instance jars as content-addressed blobs and often hardlinks them.
Overwriting mods/vansq-*.jar in place mutates (or desyncs) that blob and the launcher
refuses to start with: "needs repair or re-import".

This script:
  1. Replaces the instance jar with a unique copy (never writes through a hardlink)
  2. Registers the new sha1/sha512 in app.db and the content store
  3. Marks the file present (missing=0) and materialization as copy

Does not run Gradle. Rebuild separately when asked.
"""

from __future__ import annotations

import hashlib
import shutil
import sqlite3
import subprocess
import sys
import time
from pathlib import Path

REPO = Path(__file__).resolve().parents[1]
GRADLE_PROPS = REPO / "gradle.properties"
MODRINTH_ROOT = Path.home() / "AppData" / "Roaming" / "ModrinthApp"
DB = MODRINTH_ROOT / "app.db"
OBJECTS = MODRINTH_ROOT / "store" / "content" / "objects"
PROFILE_NAME = "Vanilla^2"
REL_PREFIX = "mods"


def load_mod_props() -> tuple[str, str]:
    props: dict[str, str] = {}
    for line in GRADLE_PROPS.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        props[key.strip()] = value.strip()
    return props["mod_id"], props["mod_version"]


def jar_running() -> bool:
    try:
        out = subprocess.check_output(
            ["tasklist", "/FI", "IMAGENAME eq javaw.exe", "/FO", "CSV", "/NH"],
            text=True,
            stderr=subprocess.DEVNULL,
        )
    except (OSError, subprocess.CalledProcessError):
        return False
    return "javaw.exe" in out.lower()


def modrinth_running() -> bool:
    try:
        out = subprocess.check_output(
            ["tasklist", "/FI", "IMAGENAME eq Modrinth App.exe", "/FO", "CSV", "/NH"],
            text=True,
            stderr=subprocess.DEVNULL,
        )
    except (OSError, subprocess.CalledProcessError):
        return False
    return "modrinth" in out.lower()


def main() -> int:
    mod_id, version = load_mod_props()
    jar_name = f"{mod_id}-{version}.jar"
    src = REPO / "build" / "libs" / jar_name
    dst = MODRINTH_ROOT / "profiles" / PROFILE_NAME / REL_PREFIX / jar_name
    relative_path = f"{REL_PREFIX}/{jar_name}"

    if not src.exists():
        print(f"missing built jar: {src}", file=sys.stderr)
        print("Rebuild first when asked, then run this script again.", file=sys.stderr)
        return 1
    if not DB.exists():
        print(f"missing Modrinth database: {DB}", file=sys.stderr)
        return 1
    if jar_running():
        print("Minecraft (javaw) is running. Close it before deploying.", file=sys.stderr)
        return 1

    data = src.read_bytes()
    sha1 = hashlib.sha1(data).hexdigest()
    sha512 = hashlib.sha512(data).hexdigest()
    size = len(data)
    now = int(time.time())
    now_ns = time.time_ns()

    blob_dir = OBJECTS / sha512[:2]
    blob_path = blob_dir / sha512
    blob_dir.mkdir(parents=True, exist_ok=True)
    if not blob_path.exists() or blob_path.stat().st_size != size:
        blob_path.write_bytes(data)

    dst.parent.mkdir(parents=True, exist_ok=True)
    if dst.exists():
        dst.unlink()
    shutil.copy2(src, dst)

    con = sqlite3.connect(str(DB), timeout=30)
    con.row_factory = sqlite3.Row
    cur = con.cursor()

    instance = cur.execute(
        "SELECT id FROM instances WHERE path=? OR name=?",
        (PROFILE_NAME, PROFILE_NAME),
    ).fetchone()
    if instance is None:
        con.close()
        print(f"copied {dst} but could not find Modrinth instance {PROFILE_NAME!r}", file=sys.stderr)
        return 1

    instance_id = instance["id"]
    file_row = cur.execute(
        "SELECT id FROM instance_files WHERE instance_id=? AND relative_path=?",
        (instance_id, relative_path),
    ).fetchone()
    if file_row is None:
        con.close()
        print(f"copied {dst}")
        print("no instance_files row to update (unmanaged jar); Modrinth hash check skipped")
        return 0

    file_id = file_row["id"]
    existing_blob = cur.execute(
        "SELECT sha512 FROM store_blobs WHERE sha512=?", (sha512,)
    ).fetchone()
    if existing_blob:
        cur.execute(
            """
            UPDATE store_blobs
            SET size=?, status='ready', modified_as=?, last_used_at=?, verified_at=?
            WHERE sha512=?
            """,
            (size, now_ns, now, now, sha512),
        )
    else:
        cur.execute(
            """
            INSERT INTO store_blobs (
                sha512, size, status, modified_as, created_at, last_used_at, verified_at, sources
            ) VALUES (?, ?, 'ready', ?, ?, ?, ?, '[]')
            """,
            (sha512, size, now_ns, now, now, now),
        )

    store_link = cur.execute(
        "SELECT file_id FROM store_instance_files WHERE file_id=?", (file_id,)
    ).fetchone()
    if store_link:
        cur.execute(
            """
            UPDATE store_instance_files
            SET blob_sha512=?, materialization_kind='copy'
            WHERE file_id=?
            """,
            (sha512, file_id),
        )
    else:
        cur.execute(
            """
            INSERT INTO store_instance_files (file_id, blob_sha512, materialization_kind)
            VALUES (?, ?, 'copy')
            """,
            (file_id, sha512),
        )

    cur.execute(
        """
        UPDATE instance_files
        SET sha1=?, size=?, missing=0, modified_at=?
        WHERE id=?
        """,
        (sha1, size, now, file_id),
    )
    con.commit()
    con.close()

    print(f"deployed {jar_name} ({size} bytes)")
    print(f"sha1 {sha1}")
    print(f"instance {dst}")
    if modrinth_running():
        print("Modrinth App is open. Fully quit it (tray too) and reopen before launching,")
        print("or it may still think this jar needs repair.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

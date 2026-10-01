"""Install 26.x CliffTree / Dungeons and Taverns packs as 1.21.1 PAXI data + resource packs.

Replaces the NeoForge wrapper jars in the Vanilla^2 mods folder. vansqtweaks is left
alone and listed last in datapack load order so its overlays still win.
"""
from __future__ import annotations

import gzip
import io
import json
import shutil
import zipfile
from pathlib import Path
from typing import Any

import nbtlib
from nbtlib import File as NbtFile
from nbtlib.tag import Compound, List as NbtList, String as NbtString

REPO = Path(__file__).resolve().parents[1]
SRC = Path(r"C:\Users\logie\Documents\!miencraft\Modpack Stuff\van squared\backport")
PROFILE = Path.home() / "AppData" / "Roaming" / "ModrinthApp" / "profiles" / "Vanilla^2"
MODS = PROFILE / "mods"
PAXI = PROFILE / "config" / "paxi"
DATA_ROOT = PAXI / "datapacks"
RES_ROOT = PAXI / "resourcepacks"

CLIFFTREE_JAR = MODS / "clifftree-3.3-neoforge-1.21.1-backport.jar"
CLIFFTREE_ZIP = SRC / "clifftree-3.3-datapack-26.2.zip"

DNT_PACKS = [
    {
        "src": SRC / "Dungeons and Taverns v6.0.1.zip",
        "data_name": "dungeons-and-taverns",
        "res_name": "dungeons-and-taverns",
        "description": "Dungeons and Taverns v6.0.1 (1.21.1 backport)",
        "jar": MODS / "dungeons-and-taverns-v4.4.4.jar",
        "with_assets": True,
    },
    {
        "src": SRC / "DnT Ancient City Overhaul 3.5.1.zip",
        "data_name": "dnt-ancient-city-overhaul",
        "res_name": "dnt-ancient-city-overhaul",
        "description": "Dungeons and Taverns Ancient City Overhaul 3.5.1 (1.21.1 backport)",
        "jar": MODS / "dungeons-and-taverns-ancient-city-overhaul-v2.jar",
        "with_assets": True,
    },
    {
        "src": SRC / "DnT Pillager Outpost Overhaul 3.4.2.zip",
        "data_name": "dnt-pillager-outpost-overhaul",
        "res_name": None,
        "description": "Dungeons and Taverns Pillager Outpost Overhaul 3.4.2 (1.21.1 backport)",
        "jar": MODS / "dungeons-and-taverns-pillager-outpost-overhaul-v2.2.jar",
        "with_assets": False,
    },
    {
        "src": SRC / "DnT Stronghold Overhaul 3.0.1.zip",
        "data_name": "dnt-stronghold-overhaul",
        "res_name": None,
        "description": "Dungeons and Taverns Stronghold Overhaul 3.0.1 (1.21.1 backport)",
        "jar": MODS / "dungeons-and-taverns-stronghold-overhaul-v2.1.f.jar",
        "with_assets": False,
    },
    {
        "src": SRC / "DnT Swamp Hut Overhau v2.4.zip",
        "data_name": "dnt-swamp-hut-overhaul",
        "res_name": None,
        "description": "Dungeons and Taverns Swamp Hut Overhaul v2.4 (1.21.1 backport)",
        "jar": MODS / "dungeons-and-taverns-swamp-hut-overhaul-v2.jar",
        "with_assets": False,
    },
]

STRIP_COMPONENTS = {
    "minecraft:tooltip_display",
    "minecraft:consumable",
    "minecraft:rarity",
    "!minecraft:equippable",
}

SKIP_LOOT_FUNCTIONS = {
    "minecraft:toggle_tooltips",
    "minecraft:filtered",
}

CMD_PRESET = {
    "dnt:chart": 1,
    "dnt:folded_chart": 2,
    "dnt:map": 3,
}

DYE_COLORS = (
    "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
    "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black",
)

DAPPLED_BLOCKS = (
    "poplar_button", "poplar_door", "poplar_fence", "poplar_fence_gate",
    "poplar_hanging_sign", "poplar_log", "poplar_planks", "poplar_pressure_plate",
    "poplar_sign", "poplar_slab", "poplar_stairs", "poplar_trapdoor",
    "poplar_wall_hanging_sign", "poplar_wall_sign", "poplar_wood",
    "stripped_poplar_log", "stripped_poplar_wood",
    "orange_poplar_leaf_bush", "orange_poplar_leaf_litter", "orange_poplar_leaves",
    "orange_poplar_sapling", "red_poplar_leaf_bush", "red_poplar_leaf_litter",
    "red_poplar_leaves", "red_poplar_sapling", "yellow_poplar_leaf_bush",
    "yellow_poplar_leaf_litter", "yellow_poplar_leaves", "yellow_poplar_sapling",
    "red_moss_block", "red_moss_carpet", "red_shrub", "shelf_mushroom",
)

# 26.3 vanilla IDs -> 1.21.1 backport mods already in the pack.
ID_REMAP = {
    "minecraft:cushion": "cushionbackport:cushion",
    "minecraft:dappled_forest": "dappled_up:dappled_forest",
    **{f"minecraft:{c}_cushion": f"cushionbackport:{c}_cushion" for c in DYE_COLORS},
    **{f"minecraft:{c}_wool_stairs": f"bwsas:{c}_wool_stairs" for c in DYE_COLORS},
    **{f"minecraft:{c}_wool_slab": f"bwsas:{c}_wool_slab" for c in DYE_COLORS},
    **{f"minecraft:{name}": f"dappled_up:{name}" for name in DAPPLED_BLOCKS},
    "minecraft:poplar_sapling": "dappled_up:orange_poplar_sapling",
    "minecraft:poplar_leaves": "dappled_up:orange_poplar_leaves",
    "minecraft:iron_chain": "minecraft:chain",
}

LOOT_LEAF_CONDITIONS = {
    "entity_properties", "damage_source_properties", "entity_scores", "killed_by_player",
    "random_chance", "random_chance_with_looting", "random_chance_with_enchanted_bonus",
    "match_tool", "table_bonus", "survives_explosion", "location_check",
    "weather_check", "time_check", "value_check", "block_state_property",
    "enchantment_active_check", "inverted", "alternative",
}

INT_PROVIDER_TYPES = {
    "uniform", "biased_to_bottom", "clamped", "clamped_normal",
    "weighted_list", "constant",
}

PROVIDER_SHORT = {
    "simple": "simple_state_provider",
    "weighted": "weighted_state_provider",
    "dual_noise": "dual_noise_provider",
    "noise": "noise_provider",
    "noise_threshold": "noise_threshold_provider",
    "randomized_int": "randomized_int_state_provider",
}

PROVIDER_PARENTS = {
    "provider", "to_place", "block_state_provider", "trunk_provider",
    "foliage_provider", "dirt_provider", "root_provider", "below_trunk_provider",
}

# 1.21.1 StringRepresentable codecs (not registry IDs) — must stay un-namespaced.
BARE_CODEC_TYPES = {
    "in_bounding_box",
    "entity_position",
}

# 1.21.1 ContextAwarePredicate fields must be JSON arrays of loot conditions.
CONTEXT_PRED_KEYS = {
    "player", "entity", "villager", "projectile", "source",
    "causing_entity", "direct_entity", "targeted_entity",
    "lightning", "parented_entity", "partner", "child",
    "location", "bystander", "lightning_bolt", "victim", "shooter",
}

BLOCKSTATE_PARENTS = {
    "output_state", "state", "block_state", "default_block", "default_fluid",
    "air_state", "barrier_state", "water_state", "lava_state",
}

ATTR_MAP = {
    "minecraft:flying_speed": "minecraft:generic.flying_speed",
    "minecraft:movement_speed": "minecraft:generic.movement_speed",
    "minecraft:max_health": "minecraft:generic.max_health",
    "minecraft:attack_damage": "minecraft:generic.attack_damage",
    "minecraft:attack_knockback": "minecraft:generic.attack_knockback",
    "minecraft:attack_speed": "minecraft:generic.attack_speed",
    "minecraft:armor": "minecraft:generic.armor",
    "minecraft:armor_toughness": "minecraft:generic.armor_toughness",
    "minecraft:knockback_resistance": "minecraft:generic.knockback_resistance",
    "minecraft:luck": "minecraft:generic.luck",
    "minecraft:follow_range": "minecraft:generic.follow_range",
    "minecraft:scale": "minecraft:generic.scale",
    "minecraft:jump_strength": "minecraft:generic.jump_strength",
    "minecraft:gravity": "minecraft:generic.gravity",
    "minecraft:step_height": "minecraft:generic.step_height",
    "minecraft:safe_fall_distance": "minecraft:generic.safe_fall_distance",
    "minecraft:fall_damage_multiplier": "minecraft:generic.fall_damage_multiplier",
}

DROP_ATTRS = {
    "minecraft:camera_distance",
    "minecraft:explosion_knockback_resistance",
    "minecraft:burning_time",
    "minecraft:visual/cloud_color",
    "minecraft:waypoint_transmit_range",
    "minecraft:waypoint_receive_range",
}

ENTITY_PRED_KEYS = {
    "minecraft:effects": "effects",
    "minecraft:equipment": "equipment",
    "minecraft:flags": "flags",
    "minecraft:location": "location",
    "minecraft:movement": "movement",
    "minecraft:passenger": "passenger",
    "minecraft:vehicle": "vehicle",
    "minecraft:targeted_entity": "targeted_entity",
    "minecraft:team": "team",
    "minecraft:nbt": "nbt",
    "minecraft:type_specific": "type_specific",
    "minecraft:slots": "slots",
    "minecraft:components": "components",
    "minecraft:periodic_tick": "periodic_tick",
    "minecraft:entity_type": "type",
    "minecraft:stepping_on": "stepping_on",
    "entity_type": "type",
}

NBT_ID_NEEDLES = (
    b"minecraft:cushion",
    b"_wool_stairs",
    b"_wool_slab",
)


def write_json(path: Path, obj: Any) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")


def remap_id_string(value: str) -> str:
    return ID_REMAP.get(value, value)


def remap_ids_in_json(obj: Any) -> Any:
    if isinstance(obj, str):
        return remap_id_string(obj)
    if isinstance(obj, list):
        return [remap_ids_in_json(x) for x in obj]
    if isinstance(obj, dict):
        return {k: remap_ids_in_json(v) for k, v in obj.items()}
    return obj


def _type_name(value: Any) -> str:
    if not isinstance(value, str):
        return ""
    return value.split(":")[-1]


def _flatten_state(value: Any) -> dict[str, Any] | None:
    if not isinstance(value, dict):
        return None
    if "Name" in value or "id" in value:
        state = dict(value)
        if "id" in state and "Name" not in state:
            state["Name"] = state.pop("id")
        if "properties" in state:
            state["Properties"] = state.pop("properties")
        return {k: v for k, v in state.items() if k in {"Name", "Properties"}}
    inner = value.get("state")
    if isinstance(inner, dict):
        return _flatten_state(inner)
    return None


def _flatten_chance(value: Any) -> float | None:
    if isinstance(value, (int, float)):
        chance = float(value)
        return min(chance / 100.0, 1.0) if chance > 1 else chance
    if not isinstance(value, dict):
        return None
    amount = value.get("amount", value)
    if isinstance(amount, dict):
        values = amount.get("values")
        if isinstance(values, list) and values:
            nums = [float(v) for v in values if isinstance(v, (int, float))]
            if nums:
                chance = max(nums)
                return min(chance / 100.0, 1.0) if chance > 1 else chance
        amount = amount.get("base", amount.get("value"))
    if isinstance(amount, (int, float)):
        chance = float(amount)
        return min(chance / 100.0, 1.0) if chance > 1 else chance
    return None


def _remap_entity_predicate(pred: dict[str, Any]) -> tuple[dict[str, Any], int | None]:
    out: dict[str, Any] = {}
    tick = pred.pop("minecraft:periodic_tick", None)
    if tick is None:
        tick = pred.pop("periodic_tick", None)
    tags = pred.pop("minecraft:entity_tags", None) or pred.pop("entity_tags", None)
    type_specific_player = pred.pop("minecraft:type_specific/player", None)
    for key, val in pred.items():
        out[ENTITY_PRED_KEYS.get(key, key)] = val
    if tags and isinstance(tags, dict):
        names = tags.get("any_of") or tags.get("all_of") or []
        if names:
            existing = out.get("nbt")
            tag_nbt = '{Tags:["' + str(names[0]) + '"]}'
            out["nbt"] = existing if isinstance(existing, str) and "Tags:" in existing else tag_nbt
    if type_specific_player and "type_specific" not in out:
        player = dict(type_specific_player) if isinstance(type_specific_player, dict) else {}
        player.pop("input", None)
        out["type_specific"] = {"type": "minecraft:player", **player}
    for nested_key in ("passenger", "vehicle", "targeted_entity", "stepping_on"):
        nested = out.get(nested_key)
        if isinstance(nested, dict):
            remapped, _ = _remap_entity_predicate(nested)
            out[nested_key] = remapped
        elif isinstance(nested, list):
            out[nested_key] = [_remap_entity_predicate(n)[0] if isinstance(n, dict) else n for n in nested]
    return out, (int(tick) if isinstance(tick, (int, float)) else None)


def convert_26x_json(obj: Any, parent: str | None = None) -> Any:
    """Rewrite leftover 26.x worldgen / enchantment JSON for 1.21.1 codecs."""
    if isinstance(obj, list):
        converted = [convert_26x_json(x, parent) for x in obj]
        return [x for x in converted if x is not None]
    if not isinstance(obj, dict):
        if parent == "count" and isinstance(obj, int) and obj > 256:
            return 256
        return obj

    items = dict(obj)

    while isinstance(items.get("type"), dict) and isinstance(items["type"].get("type"), str):
        inner = dict(items.pop("type"))
        for key, val in items.items():
            inner.setdefault(key, val)
        items = inner

    if "below_trunk_provider" in items and "dirt_provider" not in items:
        items["dirt_provider"] = items.pop("below_trunk_provider")

    if isinstance(items.get("slots"), list):
        items["slots"] = ["body" if slot == "saddle" else slot for slot in items["slots"]]

    tname = _type_name(items.get("type") or items.get("condition"))
    type_ns = str(items.get("type") or items.get("condition") or "")

    if (
        parent == "placement"
        and type_ns.startswith("lithostitched:")
        and tname not in {"condition", "offset", "noise_slope"}
    ):
        items = {"type": "lithostitched:condition", "condition": items}
        tname = "condition"
        type_ns = "lithostitched:condition"

    if isinstance(items.get("type"), str):
        raw_type = items["type"]
        raw_name = raw_type.split(":")[-1]
        if raw_name in BARE_CODEC_TYPES:
            items["type"] = raw_name
        elif ":" not in raw_type:
            items["type"] = "minecraft:" + raw_type
        type_ns = items["type"]

    if tname == "offset" and ("x" in items or "z" in items or "y" in items) and parent not in {
        "predicate", "predicates", "if_true", "target",
    }:
        items = {
            "type": "minecraft:random_offset",
            "xz_spread": items.get("x", items.get("z", 0)),
            "y_spread": items.get("y", 0),
        }
        tname = "random_offset"

    if tname == "trapezoid":
        min_v = items.get("min", items.get("min_inclusive"))
        max_v = items.get("max", items.get("max_inclusive"))
        if isinstance(min_v, dict) or isinstance(max_v, dict):
            if "min" in items:
                items["min_inclusive"] = items.pop("min")
            if "max" in items:
                items["max_inclusive"] = items.pop("max")
        else:
            if "min_inclusive" in items and "min" not in items:
                items["min"] = items.pop("min_inclusive")
            if "max_inclusive" in items and "max" not in items:
                items["max"] = items.pop("max_inclusive")
    elif tname in INT_PROVIDER_TYPES and not isinstance(items.get("max_inclusive"), dict) and "max_exclusive" not in items:
        if "min" in items and "min_inclusive" not in items and isinstance(items["min"], int):
            items["min_inclusive"] = items.pop("min")
        if "max" in items and "max_inclusive" not in items and isinstance(items["max"], int):
            items["max_inclusive"] = items.pop("max")

    if tname in PROVIDER_SHORT and (
        parent in PROVIDER_PARENTS
        or "state" in items
        or (tname == "weighted" and "entries" in items and "distribution" not in items)
    ):
        tname = PROVIDER_SHORT[tname]
        items["type"] = f"minecraft:{tname}"

    if tname == "match_tool" or _type_name(items.get("condition")) == "match_tool":
        pred = items.get("predicate")
        if isinstance(pred, dict):
            comps = pred.get("components")
            if isinstance(comps, dict) and isinstance(comps.get("minecraft:item_name"), dict):
                comps = {k: v for k, v in comps.items() if k != "minecraft:item_name"}
                pred = dict(pred)
                if comps:
                    pred["components"] = comps
                else:
                    pred.pop("components", None)
                items["predicate"] = pred

    if tname == "change_item_damage":
        items["type"] = "minecraft:damage_item"

    if "loot_tables" in items and "loot_table" not in items:
        items["loot_table"] = items.pop("loot_tables")

    if (
        parent == "conditions"
        and isinstance(items.get("blocks"), str)
        and "block" not in items
    ):
        items["block"] = items.pop("blocks")

    # Undo mistaken BlockPredicate remaps: that codec still uses "blocks".
    if parent == "block" and isinstance(items.get("block"), str) and "blocks" not in items:
        items["blocks"] = items.pop("block")

    if parent in {"source_entity", "direct_entity", "causing_entity"} and "condition" not in items:
        items, _ = _remap_entity_predicate(items)

    if tname == "explode":
        items.pop("offset", None)

    if tname == "play_sound":
        sound = items.get("sound")
        if isinstance(sound, str) and ":" not in sound:
            items["sound"] = f"minecraft:{sound}"

    if tname == "environment_attribute_check" or (
        tname == "killed_by_player" and parent == "term" and set(items) <= {"condition", "type"}
    ):
        items = {"condition": "minecraft:random_chance", "chance": 0.0}
        tname = "random_chance"

    attr = items.get("attribute")
    if isinstance(attr, str):
        if attr in DROP_ATTRS:
            return None
        items["attribute"] = ATTR_MAP.get(attr, attr)

    if tname == "rule_based_state_provider":
        fallback = _flatten_state(items.get("fallback")) or {"Name": "minecraft:dirt"}
        rules_out = []
        for rule in items.get("rules") or []:
            if not isinstance(rule, dict):
                continue
            then = _flatten_state(rule.get("then"))
            if then is None:
                continue
            rules_out.append({
                "if_true": rule.get("if_true"),
                "then": {"type": "minecraft:simple_state_provider", "state": then},
            })
        if parent == "state_provider":
            items = {
                "fallback": {"type": "minecraft:simple_state_provider", "state": fallback},
                "rules": rules_out,
            }
            tname = ""
        else:
            items = {
                "type": "minecraft:simple_state_provider",
                "state": fallback,
            }
            tname = "simple_state_provider"

    if parent == "state_provider" and isinstance(items.get("fallback"), dict):
        fallback = items["fallback"]
        if "Name" in fallback and "type" not in fallback:
            items["fallback"] = {"type": "minecraft:simple_state_provider", "state": fallback}
        for rule in items.get("rules") or []:
            if not isinstance(rule, dict):
                continue
            then = rule.get("then")
            if isinstance(then, dict) and "Name" in then and "type" not in then:
                rule["then"] = {"type": "minecraft:simple_state_provider", "state": then}

    is_spawn = parent == "spawns" and "weight" in items and "count" in items and "minCount" not in items
    if is_spawn:
        count = items.pop("count")
        if isinstance(count, int):
            items["minCount"] = count
            items["maxCount"] = count
        elif isinstance(count, dict):
            items["minCount"] = count.get("min_inclusive", count.get("min", 1))
            items["maxCount"] = count.get("max_inclusive", count.get("max", items["minCount"]))
        else:
            items["minCount"] = 1
            items["maxCount"] = 1

    is_litho = type_ns.startswith("lithostitched:")
    is_block_pred = "predicates" in items or (tname == "not" and "predicate" in items and "term" not in items)
    is_effect_all = "effects" in items and isinstance(items.get("effects"), list) and "terms" not in items and tname in {
        "all_of", "any_of",
    }
    is_loot_all = tname in {"all_of", "any_of"} and "terms" in items
    is_loot_leaf = tname in LOOT_LEAF_CONDITIONS and not is_block_pred and not is_effect_all and "processor_type" not in items

    if is_litho or is_block_pred or is_effect_all:
        if isinstance(items.get("condition"), str):
            items["type"] = items.pop("condition")
            if ":" not in items["type"]:
                prefix = "lithostitched:" if is_litho else "minecraft:"
                items["type"] = prefix + items["type"]
    elif is_loot_all or is_loot_leaf:
        if "type" in items and "condition" not in items:
            items["condition"] = items.pop("type")
            if isinstance(items["condition"], str) and ":" not in items["condition"]:
                items["condition"] = "minecraft:" + items["condition"]

    if tname == "random_chance" or _type_name(items.get("condition")) == "random_chance":
        chance = _flatten_chance(items.get("chance"))
        if chance is not None:
            items["chance"] = chance

    if tname == "time_check" or _type_name(items.get("condition")) == "time_check":
        items.pop("clock", None)

    if tname == "damage_source_properties" or _type_name(items.get("condition")) == "damage_source_properties":
        pred = items.get("predicate")
        if isinstance(pred, dict):
            pred = dict(pred)
            pred.pop("is_direct", None)
            tags = pred.get("tags")
            if isinstance(tags, list):
                for tag in tags:
                    if isinstance(tag, dict) and isinstance(tag.get("id"), str) and tag["id"].startswith("#"):
                        tag["id"] = tag["id"][1:]
            items["predicate"] = pred

    if tname == "entity_properties" or _type_name(items.get("condition")) == "entity_properties":
        pred = items.get("predicate")
        if isinstance(pred, dict):
            pred, tick = _remap_entity_predicate(dict(pred))
            items.pop("type", None)
            items["condition"] = "minecraft:entity_properties"
            items["predicate"] = pred
            if tick:
                chance = 1.0 / max(tick, 1)
                if pred:
                    items = {
                        "condition": "minecraft:all_of",
                        "terms": [
                            items,
                            {"condition": "minecraft:random_chance", "chance": chance},
                        ],
                    }
                else:
                    items = {"condition": "minecraft:random_chance", "chance": chance}

    looks_like_blockstate = (
        parent != "icon"
        and "id" in items
        and "count" not in items
        and "components" not in items
        and "attribute" not in items
        and "operation" not in items
        and (
            "properties" in items
            or "Properties" in items
            or parent in BLOCKSTATE_PARENTS
            or set(items) <= {"id", "properties", "Properties", "Name"}
        )
    )
    if looks_like_blockstate:
        items["Name"] = items.pop("id")
        if "properties" in items:
            items["Properties"] = items.pop("properties")

    if parent == "icon" and "Name" in items and "id" not in items:
        items["id"] = items.pop("Name")

    name = items.get("Name")
    if isinstance(name, str) and ":" not in name:
        items["Name"] = f"minecraft:{name}"

    blocks = items.get("blocks")
    if isinstance(blocks, str) and ":" not in blocks:
        items["blocks"] = f"minecraft:{blocks}"
    elif isinstance(blocks, list):
        items["blocks"] = [
            f"minecraft:{b}" if isinstance(b, str) and ":" not in b else b
            for b in blocks
        ]

    ptype = items.get("predicate_type")
    if isinstance(ptype, str) and ":" not in ptype:
        items["predicate_type"] = f"minecraft:{ptype}"

    out: dict[str, Any] = {}
    for key, val in items.items():
        converted = convert_26x_json(val, key)
        if converted is None:
            continue
        if key in {"minecraft:attributes", "attributes"} and converted == []:
            continue
        if key in CONTEXT_PRED_KEYS and _is_loot_condition(converted):
            cond_name = _type_name(converted.get("condition") or converted.get("type"))
            if cond_name == "all_of" and isinstance(converted.get("terms"), list):
                converted = converted["terms"]
            else:
                converted = [converted]
        out[key] = converted
    return out


def _is_loot_condition(obj: Any) -> bool:
    if not isinstance(obj, dict):
        return False
    if isinstance(obj.get("condition"), str):
        return True
    tname = _type_name(obj.get("type"))
    return tname in LOOT_LEAF_CONDITIONS or tname in {"all_of", "any_of", "inverted", "reference"}


def wrap_flattened_configured_feature(obj: Any) -> Any:
    """26.x inlined ConfiguredFeature config at the root; 1.21.1 still wants {type, config}."""
    if not isinstance(obj, dict) or "config" in obj:
        return obj
    ftype = obj.get("type")
    if not isinstance(ftype, str):
        return obj
    config = {k: v for k, v in obj.items() if k != "type"}
    if not config:
        return obj
    if ":" not in ftype:
        ftype = f"minecraft:{ftype}"
    return {"type": ftype, "config": config}


def datapack_rel(rel: str) -> str:
    return rel.replace("\\", "/").replace("/worldgen/feature/", "/worldgen/configured_feature/")


def relocate_worldgen_features(root: Path) -> int:
    moved = 0
    for path in list(root.rglob("*.json")):
        parts = path.parts
        try:
            idx = parts.index("worldgen")
        except ValueError:
            continue
        if idx + 1 >= len(parts) or parts[idx + 1] != "feature":
            continue
        dest_parts = list(parts)
        dest_parts[idx + 1] = "configured_feature"
        dest = Path(*dest_parts)
        try:
            obj = json.loads(path.read_text(encoding="utf-8"))
        except Exception:
            continue
        obj = wrap_flattened_configured_feature(convert_26x_json(remap_ids_in_json(obj)))
        dest.parent.mkdir(parents=True, exist_ok=True)
        write_json(dest, obj)
        path.unlink()
        moved += 1
    for folder in list(root.rglob("worldgen")):
        feature_dir = folder / "feature"
        if feature_dir.is_dir() and not any(feature_dir.iterdir()):
            feature_dir.rmdir()
    return moved


def _simple_block_feature(block: str, props: dict[str, str] | None = None) -> dict[str, Any]:
    state: dict[str, Any] = {"Name": block}
    if props:
        state["Properties"] = props
    return {
        "type": "minecraft:simple_block",
        "config": {
            "to_place": {
                "type": "minecraft:simple_state_provider",
                "state": state,
            }
        },
    }


def _wildflower_feature() -> dict[str, Any]:
    """26.x minecraft:wildflower inner feature, using VanillaBackport's wildflowers block."""
    entries = []
    for amount in ("1", "2", "3", "4"):
        for facing in ("north", "east", "south", "west"):
            entries.append({
                "data": {
                    "Name": "minecraft:wildflowers",
                    "Properties": {
                        "facing": facing,
                        "flower_amount": amount,
                    },
                },
                "weight": 1,
            })
    return {
        "type": "minecraft:simple_block",
        "config": {
            "to_place": {
                "type": "minecraft:weighted_state_provider",
                "entries": entries,
            }
        },
    }


def _sapling_placed_feature(configured_id: str, sapling: str) -> dict[str, Any]:
    return {
        "feature": configured_id,
        "placement": [
            {
                "type": "minecraft:block_predicate_filter",
                "predicate": {
                    "type": "minecraft:would_survive",
                    "state": {
                        "Name": sapling,
                        "Properties": {"stage": "0"},
                    },
                },
            }
        ],
    }


def fill_missing_26x_vanilla_features() -> int:
    """Keep CliffTree's 26.x feature IDs, pointed at VanillaBackport / 1.21.1 blocks."""
    written = 0
    mc = DATA_ROOT / "clifftree" / "data" / "minecraft" / "worldgen"
    configured = mc / "configured_feature"
    placed = mc / "placed_feature"
    configured.mkdir(parents=True, exist_ok=True)
    placed.mkdir(parents=True, exist_ok=True)

    stubs = {
        "tall_grass": _simple_block_feature("minecraft:tall_grass", {"half": "lower"}),
        "berry_bush": _simple_block_feature("minecraft:sweet_berry_bush", {"age": "3"}),
        "firefly_bush": _simple_block_feature("minecraft:firefly_bush"),
        "wildflower": _wildflower_feature(),
    }
    for name, obj in stubs.items():
        path = configured / f"{name}.json"
        write_json(path, obj)
        written += 1

    oak_leaf = configured / "oak_bees_0002_leaf_litter.json"
    fancy_src = DATA_ROOT / "clifftree" / "data" / "clifftree" / "worldgen" / "configured_feature" / "fancy_oak_bees_0002.json"
    fancy_dest = configured / "fancy_oak_bees_0002_leaf_litter.json"
    if oak_leaf.exists() and fancy_src.exists() and not fancy_dest.exists():
        oak = json.loads(oak_leaf.read_text(encoding="utf-8"))
        fancy = json.loads(fancy_src.read_text(encoding="utf-8"))
        litter = [
            deco for deco in oak.get("config", {}).get("decorators", [])
            if isinstance(deco, dict) and deco.get("type") == "minecraft:place_on_ground"
        ]
        fancy.setdefault("config", {}).setdefault("decorators", [])
        fancy["config"]["decorators"] = litter + list(fancy["config"]["decorators"])
        write_json(fancy_dest, fancy)
        written += 1

    saplings = {
        "birch_bees_0002_leaf_litter": "minecraft:birch_sapling",
        "oak_bees_0002_leaf_litter": "minecraft:oak_sapling",
        "fancy_oak_bees_0002_leaf_litter": "minecraft:oak_sapling",
    }
    for name, sapling in saplings.items():
        path = placed / f"{name}.json"
        if not path.exists():
            write_json(path, _sapling_placed_feature(f"minecraft:{name}", sapling))
            written += 1
    return written


def remap_ids_in_nbt(obj: Any) -> bool:
    changed = False
    if isinstance(obj, Compound):
        for key in list(obj.keys()):
            val = obj[key]
            if isinstance(val, NbtString):
                new = remap_id_string(str(val))
                if new != str(val):
                    obj[key] = NbtString(new)
                    changed = True
            elif remap_ids_in_nbt(val):
                changed = True
    elif isinstance(obj, NbtList):
        for i, val in enumerate(obj):
            if isinstance(val, NbtString):
                new = remap_id_string(str(val))
                if new != str(val):
                    obj[i] = NbtString(new)
                    changed = True
            elif remap_ids_in_nbt(val):
                changed = True
    return changed


def convert_structure_nbt(raw: bytes) -> bytes:
    gzipped = raw[:2] == b"\x1f\x8b"
    payload = gzip.decompress(raw) if gzipped else raw
    if not any(needle in payload for needle in NBT_ID_NEEDLES):
        return raw
    nbt = NbtFile.parse(io.BytesIO(payload))
    if not remap_ids_in_nbt(nbt):
        return raw
    buf = io.BytesIO()
    nbt.write(buf)
    out = buf.getvalue()
    return gzip.compress(out) if gzipped else out


def patch_installed_dnt_ids() -> None:
    packs = [p["data_name"] for p in DNT_PACKS]
    nbt_changed = 0
    json_changed = 0
    for name in packs:
        root = DATA_ROOT / name
        if not root.exists():
            continue
        for path in root.rglob("*"):
            if not path.is_file():
                continue
            if path.suffix == ".nbt":
                raw = path.read_bytes()
                converted = convert_structure_nbt(raw)
                if converted != raw:
                    path.write_bytes(converted)
                    nbt_changed += 1
            elif path.suffix == ".json":
                text = path.read_text(encoding="utf-8")
                if "minecraft:cushion" not in text and "_wool_stairs" not in text and "_wool_slab" not in text:
                    continue
                try:
                    obj = json.loads(text)
                except Exception:
                    continue
                remapped = remap_ids_in_json(obj)
                new_text = json.dumps(remapped, indent=2, ensure_ascii=False) + "\n"
                if new_text != text:
                    path.write_text(new_text, encoding="utf-8")
                    json_changed += 1
    print(f"remapped installed DnT ids: {nbt_changed} nbt, {json_changed} json")


def patch_installed_26x_schema() -> None:
    packs = ["clifftree", *[p["data_name"] for p in DNT_PACKS]]
    moved = 0
    extra = 0
    changed = 0
    funcs = 0
    for name in packs:
        root = DATA_ROOT / name
        if not root.exists():
            continue
        moved += relocate_worldgen_features(root)
        for path in root.rglob("*.json"):
            try:
                obj = json.loads(path.read_text(encoding="utf-8"))
            except Exception:
                continue
            converted = convert_26x_json(remap_ids_in_json(obj))
            parts = path.parts
            if "configured_feature" in parts:
                converted = wrap_flattened_configured_feature(converted)
            new_text = json.dumps(converted, indent=2, ensure_ascii=False) + "\n"
            old_text = path.read_text(encoding="utf-8")
            if new_text != old_text:
                path.write_text(new_text, encoding="utf-8")
                changed += 1
        for path in root.rglob("*.mcfunction"):
            old_text = path.read_text(encoding="utf-8")
            new_text = convert_mcfunction(old_text)
            if new_text != old_text:
                path.write_text(new_text, encoding="utf-8")
                funcs += 1
    extra = fill_missing_26x_vanilla_features()
    print(
        f"converted 26.x schema in {changed} json files and {funcs} functions; "
        f"relocated {moved} features; added {extra} missing vanilla feature files"
    )



def extract_member(z: zipfile.ZipFile, name: str, dest: Path) -> None:
    dest.parent.mkdir(parents=True, exist_ok=True)
    dest.write_bytes(z.read(name))


def cmd_id(table: dict[str, int], key: str) -> int:
    if key not in table:
        table[key] = max(table.values(), default=0) + 1
    return table[key]


def convert_custom_model_data(value: Any, cmds: dict[str, int]) -> Any:
    if isinstance(value, int):
        return value
    if isinstance(value, dict):
        strings = value.get("strings") or []
        if strings:
            return cmd_id(cmds, str(strings[0]))
        floats = value.get("floats") or []
        if floats:
            return int(floats[0])
    if isinstance(value, str):
        return cmd_id(cmds, value)
    return 0


def convert_components(components: dict[str, Any], cmds: dict[str, int]) -> dict[str, Any]:
    out: dict[str, Any] = {}
    item_name = components.get("minecraft:item_name")
    cmd_val = components.get("minecraft:custom_model_data")
    name_key = None
    if isinstance(item_name, dict):
        name_key = item_name.get("translate")
    cmd_from_strings = isinstance(cmd_val, dict) and bool(cmd_val.get("strings"))
    if cmd_from_strings:
        out["minecraft:custom_model_data"] = convert_custom_model_data(cmd_val, cmds)
    elif isinstance(name_key, str) and name_key.startswith("item.dnt."):
        out["minecraft:custom_model_data"] = cmd_id(cmds, name_key)
    elif cmd_val is not None:
        out["minecraft:custom_model_data"] = convert_custom_model_data(cmd_val, cmds)
    for key, val in components.items():
        if key in STRIP_COMPONENTS or key.startswith("!"):
            if key == "minecraft:consumable" and "minecraft:food" not in out:
                out["minecraft:food"] = {
                    "nutrition": 0,
                    "saturation": 0.0,
                    "can_always_eat": True,
                    "eat_seconds": 36000.0,
                }
            continue
        if key == "minecraft:custom_model_data":
            continue
        out[key] = convert_node(val, cmds, "generic")
    return out


def looks_like_function(obj: dict[str, Any]) -> bool:
    t = obj.get("type") or obj.get("function")
    if not isinstance(t, str):
        return False
    name = t.split(":")[-1]
    return name in {
        "set_count", "set_damage", "set_potion", "set_components", "set_custom_data",
        "set_name", "set_lore", "set_nbt", "set_enchantments", "enchant_with_levels",
        "enchant_randomly", "exploration_map", "fill_player_head", "furnace_smelt",
        "limit_count", "looting_enchant", "set_attributes", "set_banner_pattern",
        "set_contents", "set_instrument", "set_loot_table", "set_stew_effect",
        "copy_name", "copy_nbt", "copy_state", "copy_custom_data", "copy_components",
        "explosion_decay", "apply_bonus", "reference", "sequence", "filtered",
        "toggle_tooltips", "set_ominous_bottle_amplifier", "set_fireworks",
        "set_writable_book_pages", "set_written_book_pages", "set_item",
        "modify_contents", "filtered", "set_custom_model_data",
    }


def looks_like_condition(obj: dict[str, Any]) -> bool:
    t = obj.get("type") or obj.get("condition")
    if not isinstance(t, str):
        return False
    name = t.split(":")[-1]
    return name in {
        "random_chance", "random_chance_with_looting", "killed_by_player",
        "entity_properties", "entity_scores", "block_state_property",
        "match_tool", "table_bonus", "survives_explosion", "damage_source_properties",
        "location_check", "weather_check", "time_check", "value_check",
        "reference", "any_of", "all_of", "inverted", "alternative",
        "killed_by_player", "random_chance_with_enchanted_bonus",
    } or t.startswith("minecraft:") and name in {"any_of", "all_of", "inverted"}


def convert_function(obj: Any, cmds: dict[str, int]) -> Any:
    if not isinstance(obj, dict):
        return convert_node(obj, cmds, "generic")
    t = obj.get("type") or obj.get("function")
    if t in SKIP_LOOT_FUNCTIONS:
        return None
    out: dict[str, Any] = {}
    for key, val in obj.items():
        if key == "type":
            continue
        if key == "components" and isinstance(val, dict):
            out[key] = convert_components(val, cmds)
        else:
            out[key] = convert_node(val, cmds, "function" if key in {"functions", "on_pass", "on_fail"} else "generic")
    if t:
        out["function"] = t
    return out


def convert_condition(obj: Any, cmds: dict[str, int]) -> Any:
    if not isinstance(obj, dict):
        return convert_node(obj, cmds, "generic")
    t = obj.get("type") or obj.get("condition")
    out: dict[str, Any] = {}
    for key, val in obj.items():
        if key == "type":
            continue
        out[key] = convert_node(val, cmds, "condition" if key in {"terms", "conditions"} else "generic")
    if t:
        out["condition"] = t
    return out


def convert_entry(obj: dict[str, Any], cmds: dict[str, int]) -> dict[str, Any]:
    out: dict[str, Any] = {}
    for key, val in obj.items():
        if key == "modifier":
            funcs = val if isinstance(val, list) else [val]
            converted = [convert_function(f, cmds) for f in funcs if isinstance(f, (dict, str))]
            converted = [f for f in converted if f]
            if converted:
                out.setdefault("functions", []).extend(converted)
            continue
        if key == "functions":
            converted = [convert_function(f, cmds) for f in val]
            converted = [f for f in converted if f]
            out.setdefault("functions", []).extend(converted)
            continue
        if key == "condition" and isinstance(val, dict):
            out.setdefault("conditions", []).append(convert_condition(val, cmds))
            continue
        if key == "conditions":
            out.setdefault("conditions", []).extend(convert_condition(c, cmds) for c in val)
            continue
        if key == "value" and obj.get("type") in {"minecraft:loot_table", "loot_table"}:
            out["name"] = val
            continue
        if key == "children" and isinstance(val, list):
            out[key] = [convert_entry(c, cmds) if isinstance(c, dict) else c for c in val]
            continue
        if key == "entries" and isinstance(val, list):
            out[key] = [convert_entry(c, cmds) if isinstance(c, dict) else c for c in val]
            continue
        out[key] = convert_node(val, cmds, "generic")
    if out.get("type") == "item":
        out["type"] = "minecraft:item"
    return out


def convert_node(obj: Any, cmds: dict[str, int], ctx: str) -> Any:
    if isinstance(obj, list):
        if ctx == "function":
            out = [convert_function(x, cmds) for x in obj]
            return [x for x in out if x]
        if ctx == "condition":
            return [convert_condition(x, cmds) for x in obj]
        return [convert_node(x, cmds, ctx) for x in obj]
    if not isinstance(obj, dict):
        return obj

    # Item modifier root
    if ctx == "item_modifier":
        t = obj.get("type") or obj.get("function")
        if t == "minecraft:sequence" or "functions" in obj:
            funcs = obj.get("functions") or obj.get("modifier") or []
            converted = [convert_function(f, cmds) for f in funcs]
            return [f for f in converted if f]
        converted = convert_function(obj, cmds)
        return [converted] if converted else []

    if "spawn_overrides" in obj:
        obj = dict(obj)
        overrides = {}
        for category, spec in obj.get("spawn_overrides", {}).items():
            if not isinstance(spec, dict):
                overrides[category] = spec
                continue
            spawns = spec.get("spawns")
            if isinstance(spawns, list):
                spec = dict(spec)
                spec["spawns"] = [
                    s for s in spawns
                    if not (isinstance(s, dict) and str(s.get("type", "")).endswith("nautilus"))
                ]
            overrides[category] = spec
        obj["spawn_overrides"] = overrides

    if ctx == "entry":
        return convert_entry(obj, cmds)

    keys = set(obj.keys())
    if "pools" in keys or obj.get("type") in {"minecraft:chest", "minecraft:generic", "minecraft:archaeology", "minecraft:gift"}:
        return convert_loot_table(obj, cmds)

    out: dict[str, Any] = {}
    for key, val in obj.items():
        if key == "minecraft:custom_model_data":
            out[key] = convert_custom_model_data(val, cmds)
            continue
        if key == "components" and isinstance(val, dict):
            out[key] = convert_components(val, cmds)
            continue
        if key == "zombie_nautilus" or (isinstance(val, str) and val == "minecraft:zombie_nautilus"):
            continue
        child_ctx = ctx
        if key in {"functions", "modifier"} and isinstance(val, list):
            child_ctx = "function"
        elif key in {"conditions"} and isinstance(val, list):
            child_ctx = "condition"
        elif key in {"entries", "children"}:
            child_ctx = "entry"
        out[key] = convert_node(val, cmds, child_ctx)
    return out


def convert_loot_table(obj: dict[str, Any], cmds: dict[str, int]) -> dict[str, Any]:
    out: dict[str, Any] = {}
    for key, val in obj.items():
        if key == "modifier":
            if val in {"nova_structures:loot_modifier", "loot_modifier"}:
                continue
            if isinstance(val, str):
                out.setdefault("functions", []).append({
                    "function": "minecraft:reference",
                    "name": val,
                })
            elif isinstance(val, list):
                converted = [convert_function(f, cmds) for f in val]
                converted = [f for f in converted if f]
                if converted:
                    out.setdefault("functions", []).extend(converted)
            elif isinstance(val, dict):
                converted = convert_function(val, cmds)
                if converted:
                    out.setdefault("functions", []).append(converted)
            continue
        if key == "pools":
            pools = []
            for pool in val:
                p = {}
                for pk, pv in pool.items():
                    if pk == "entries":
                        p[pk] = [convert_entry(e, cmds) if isinstance(e, dict) else e for e in pv]
                    elif pk == "conditions":
                        p[pk] = [convert_condition(c, cmds) for c in pv]
                    elif pk == "condition" and isinstance(pv, dict):
                        p.setdefault("conditions", []).append(convert_condition(pv, cmds))
                    elif pk == "functions" or pk == "modifier":
                        funcs = pv if isinstance(pv, list) else [pv]
                        converted = [convert_function(f, cmds) for f in funcs]
                        converted = [f for f in converted if f]
                        if converted:
                            p.setdefault("functions", []).extend(converted)
                    else:
                        p[pk] = convert_node(pv, cmds, "generic")
                pools.append(p)
            out[key] = pools
            continue
        if key == "functions":
            converted = [convert_function(f, cmds) for f in val]
            converted = [f for f in converted if f]
            out.setdefault("functions", []).extend(converted)
            continue
        out[key] = convert_node(val, cmds, "generic")
    return out


def convert_mcfunction(text: str, cmds: dict[str, int] | None = None) -> str:
    for old, new in ATTR_MAP.items():
        text = text.replace(f'id:"{old}"', f'id:"{new}"')
        text = text.replace(f"id:'{old}'", f"id:'{new}'")
    if cmds:
        for raw, num in cmds.items():
            text = text.replace(
                f'minecraft:custom_model_data={{strings:["{raw}"]}}',
                f"minecraft:custom_model_data={num}",
            )
            text = text.replace(
                f"minecraft:custom_model_data={{strings:['{raw}']}}",
                f"minecraft:custom_model_data={num}",
            )
    return text


def convert_json_bytes(raw: bytes, rel: str, cmds: dict[str, int]) -> bytes:
    try:
        obj = json.loads(raw.decode("utf-8"))
    except Exception:
        return raw
    ctx = "generic"
    if "/loot_table/" in rel or rel.endswith("loot_table.json"):
        ctx = "loot"
    elif "/item_modifier/" in rel:
        ctx = "item_modifier"
    if ctx == "loot" and isinstance(obj, dict):
        obj = convert_loot_table(obj, cmds)
    else:
        obj = convert_node(obj, cmds, ctx)
    obj = remap_ids_in_json(obj)
    obj = convert_26x_json(obj)
    if "/worldgen/feature/" in rel.replace("\\", "/") or "/worldgen/configured_feature/" in rel.replace("\\", "/"):
        obj = wrap_flattened_configured_feature(obj)
    return (json.dumps(obj, indent=2, ensure_ascii=False) + "\n").encode("utf-8")


def datapack_mcmeta(description: str, extra_overlays: list[dict[str, Any]] | None = None) -> dict[str, Any]:
    overlays = extra_overlays or []
    pack: dict[str, Any] = {
        "pack": {
            "pack_format": 48,
            "description": description,
        }
    }
    if overlays:
        pack["overlays"] = {"entries": overlays}
    return pack


def resource_mcmeta(description: str) -> dict[str, Any]:
    return {
        "pack": {
            "pack_format": 34,
            "description": description,
        }
    }


def wipe(path: Path) -> None:
    if path.exists():
        shutil.rmtree(path)
    path.mkdir(parents=True, exist_ok=True)


def install_clifftree() -> None:
    if not CLIFFTREE_JAR.exists():
        data_dir = DATA_ROOT / "clifftree"
        if data_dir.exists() and (data_dir / "pack.mcmeta").exists():
            print("clifftree already installed (jar gone); leaving in place")
            return
        raise FileNotFoundError(f"Missing {CLIFFTREE_JAR}")
    data_dir = DATA_ROOT / "clifftree"
    res_dir = RES_ROOT / "clifftree"
    wipe(data_dir)
    wipe(res_dir)

    with zipfile.ZipFile(CLIFFTREE_JAR) as zo:
        for name in zo.namelist():
            rel = name.replace("\\", "/")
            if rel.endswith("/") or rel.startswith("META-INF/"):
                continue
            if rel.startswith("assets/"):
                extract_member(zo, name, res_dir / rel)
            elif rel in {"pack.png", "clifftree_snowcapped.json"}:
                extract_member(zo, name, data_dir / rel)
                extract_member(zo, name, res_dir / rel)
            elif rel == "pack.mcmeta":
                continue
            else:
                extract_member(zo, name, data_dir / rel)
        pack_meta = json.loads(zo.read("pack.mcmeta").decode("utf-8"))
        pack_meta["pack"]["pack_format"] = 48
        pack_meta["pack"]["description"] = "CliffTree 3.3 (1.21.1 backport)"
        # Keep galosphere + lithostitched NeoForge overlays from the working jar.

    with zipfile.ZipFile(CLIFFTREE_ZIP) as zn:
        new_files = {n.replace("\\", "/") for n in zn.namelist() if not n.endswith("/")}
        skip_prefixes = (
            "data/minecraft/timeline/",
            "overlay.sky_biomes/",
            "data/clifftree/worldgen/biome/sky/",
            "data/minecraft/dimension/",
            "data/minecraft/dimension_type/",
            "META-INF/",
            "pack.mcmeta",
        )
        for rel in sorted(new_files):
            if any(rel.startswith(p) for p in skip_prefixes):
                continue
            if rel.startswith("data/clifftree/worldgen/biome/") and "/sky/" not in rel:
                continue
            if rel.startswith("data/minecraft/worldgen/noise_settings/"):
                continue
            dest_data = data_dir / datapack_rel(rel)
            dest_res = res_dir / rel
            if rel.startswith("assets/"):
                extract_member(zn, rel, dest_res)
                continue
            if rel.startswith("overlay.compat/") or not dest_data.exists():
                extract_member(zn, rel, dest_data)

        # Merge 26.2 feature lists into the 1.21.1 biomes (which lack the attributes schema).
        for rel in sorted(new_files):
            if not rel.startswith("data/clifftree/worldgen/biome/") or not rel.endswith(".json"):
                continue
            if "/sky/" in rel:
                continue
            old_path = data_dir / rel
            if not old_path.exists():
                continue
            old_biome = json.loads(old_path.read_text(encoding="utf-8"))
            new_biome = json.loads(zn.read(rel).decode("utf-8"))
            old_features = old_biome.setdefault("features", [])
            changed = False
            for i, step in enumerate(new_biome.get("features", [])):
                while len(old_features) <= i:
                    old_features.append([])
                for feat in step:
                    if feat not in old_features[i]:
                        old_features[i].append(feat)
                        changed = True
            if changed:
                write_json(old_path, old_biome)

        if any(n.startswith("overlay.compat/") for n in new_files):
            pack_meta.setdefault("overlays", {}).setdefault("entries", [])
            entries = pack_meta["overlays"]["entries"]
            if not any(e.get("directory") == "overlay.compat" for e in entries):
                entries.append({"directory": "overlay.compat", "formats": [48, 48]})

    write_json(data_dir / "pack.mcmeta", pack_meta)
    litho = data_dir / "overlay.lithostitched" / "data"
    if litho.exists():
        shutil.copytree(litho, data_dir / "data", dirs_exist_ok=True)
    write_json(res_dir / "pack.mcmeta", resource_mcmeta("CliffTree 3.3 assets (1.21.1 backport)"))
    print("installed clifftree")


def copy_zip_as_paxi(src: Path, data_name: str, res_name: str | None, description: str, with_assets: bool, cmds: dict[str, int]) -> bool:
    data_dir = DATA_ROOT / data_name
    wipe(data_dir)
    res_dir = None
    has_assets = False
    if with_assets and res_name:
        res_dir = RES_ROOT / res_name
        wipe(res_dir)

    with zipfile.ZipFile(src) as z:
        for name in z.namelist():
            rel = name.replace("\\", "/")
            if rel.endswith("/") or rel.startswith("META-INF/"):
                continue
            data = z.read(name)
            if rel == "pack.mcmeta":
                continue
            if rel.startswith("assets/"):
                has_assets = True
                if res_dir is None:
                    res_dir = RES_ROOT / (res_name or data_name)
                    wipe(res_dir)
                # 1.21.4+ item model defs are converted separately.
                if rel.startswith("assets/minecraft/items/"):
                    continue
                if rel.endswith(".json"):
                    data = convert_json_bytes(data, rel, cmds)
                (res_dir / rel).parent.mkdir(parents=True, exist_ok=True)
                (res_dir / rel).write_bytes(data)
                continue
            if rel.endswith(".mcfunction"):
                text = convert_mcfunction(data.decode("utf-8", "ignore"), cmds)
                dest = data_dir / datapack_rel(rel)
                dest.parent.mkdir(parents=True, exist_ok=True)
                dest.write_text(text, encoding="utf-8")
                continue
            if rel.endswith(".json"):
                data = convert_json_bytes(data, rel, cmds)
            elif rel.endswith(".nbt"):
                data = convert_structure_nbt(data)
            dest = data_dir / datapack_rel(rel)
            dest.parent.mkdir(parents=True, exist_ok=True)
            dest.write_bytes(data)

        write_json(data_dir / "pack.mcmeta", datapack_mcmeta(description))
        if has_assets and res_dir is not None:
            if (data_dir / "pack.png").exists() and not (res_dir / "pack.png").exists():
                shutil.copy2(data_dir / "pack.png", res_dir / "pack.png")
            write_json(res_dir / "pack.mcmeta", resource_mcmeta(description + " assets"))
            build_item_overrides(z, res_dir, cmds)
    print("installed", data_name, "assets" if has_assets else "data-only")
    return has_assets


def build_item_overrides(z: zipfile.ZipFile, res_dir: Path, cmds: dict[str, int]) -> None:
    """Turn 26.x items/*.json select models into 1.21.1 predicate overrides."""
    for name in z.namelist():
        rel = name.replace("\\", "/")
        if not rel.startswith("assets/minecraft/items/") or not rel.endswith(".json"):
            continue
        item = Path(rel).stem
        try:
            spec = json.loads(z.read(name).decode("utf-8"))
        except Exception:
            continue
        model = spec.get("model") or spec
        cases = []
        fallback = None
        if model.get("type") == "minecraft:select":
            cases = model.get("cases") or []
            fallback = model.get("fallback")
        if not cases:
            continue
        overrides = []
        for case in cases:
            when = case.get("when")
            inner = case.get("model") or {}
            model_path = inner.get("model")
            if not model_path:
                continue
            key = None
            if isinstance(when, str):
                key = when
            elif isinstance(when, dict):
                key = when.get("translate") or when.get("fallback")
            if not key:
                continue
            overrides.append({
                "predicate": {"custom_model_data": cmd_id(cmds, str(key))},
                "model": model_path,
            })
        if not overrides:
            continue
        parent = "minecraft:item/generated"
        textures = {"layer0": f"minecraft:item/{item}"}
        if isinstance(fallback, dict) and fallback.get("model"):
            # Keep vanilla texture as the un-overridden model.
            pass
        out = {
            "parent": parent,
            "textures": textures,
            "overrides": overrides,
        }
        write_json(res_dir / "assets" / "minecraft" / "models" / "item" / f"{item}.json", out)


def update_load_orders(resource_packs: list[str]) -> None:
    data_order = [
        "clifftree",
        "dungeons-and-taverns",
        "dnt-ancient-city-overhaul",
        "dnt-pillager-outpost-overhaul",
        "dnt-stronghold-overhaul",
        "dnt-swamp-hut-overhaul",
        "vansqtweaks",
        "lootr_no_advancements",
        "lootr_no_suspicious_blocks",
    ]
    write_json(PAXI / "datapack_load_order.json", {"loadOrder": data_order})

    res_order_path = PAXI / "resourcepack_load_order.json"
    existing = []
    if res_order_path.exists():
        existing = json.loads(res_order_path.read_text(encoding="utf-8")).get("loadOrder") or []
    # Keep Fogulous first if listed, insert backports before vansqresources.
    head = [n for n in existing if n.lower().startswith("fogulous")]
    tail = [n for n in existing if n not in head and n not in resource_packs]
    if "vansqresources" in tail:
        tail.remove("vansqresources")
        ordered = head + resource_packs + tail + ["vansqresources"]
    else:
        ordered = head + resource_packs + tail
    # de-dupe preserving order
    seen = set()
    unique = []
    for n in ordered:
        if n not in seen:
            seen.add(n)
            unique.append(n)
    write_json(res_order_path, {"loadOrder": unique})


def main() -> None:
    cmds = dict(CMD_PRESET)
    install_clifftree()
    res_packs = ["clifftree"]
    for pack in DNT_PACKS:
        has_assets = copy_zip_as_paxi(
            pack["src"],
            pack["data_name"],
            pack["res_name"],
            pack["description"],
            pack["with_assets"],
            cmds,
        )
        if has_assets and pack["res_name"]:
            res_packs.append(pack["res_name"])

    update_load_orders(res_packs)

    removed = []
    for jar in [CLIFFTREE_JAR, *[p["jar"] for p in DNT_PACKS]]:
        if jar.exists():
            jar.unlink()
            removed.append(jar.name)
    print("removed jars:", ", ".join(removed) or "(none)")
    print("cmd map size", len(cmds))
    patch_installed_dnt_ids()
    patch_installed_26x_schema()
    cmd_map_path = REPO / "_extract_tmp" / "dnt_cmd_map.json"
    cmd_map_path.parent.mkdir(parents=True, exist_ok=True)
    write_json(cmd_map_path, cmds)


if __name__ == "__main__":
    main()

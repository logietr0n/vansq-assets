package com.vansqmod.mixin.moonlight;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Moonlight 3.3.0 ships {@code executableName} but not {@code findExecutable}.
 * Vista 5.5.5 calls the latter during mod construct.
 */
@Mixin(targets = "net.mehvahdjukaar.moonlight.api.util.OsType", remap = false)
public abstract class MoonlightOsTypeFindExecutableMixin {

    @Shadow(remap = false)
    public abstract String executableName(String baseName);

    @Shadow(remap = false)
    public abstract boolean isMac();

    public Path findExecutable(String baseName) {
        String fileName = this.executableName(baseName);
        String pathEnv = System.getenv("PATH");
        if (pathEnv != null && !pathEnv.isEmpty()) {
            Path found = vansqmod$searchDirs(fileName, pathEnv.split(java.io.File.pathSeparator));
            if (found != null) {
                return found;
            }
        }
        if (this.isMac()) {
            return vansqmod$searchDirs(fileName, new String[]{"/opt/homebrew/bin", "/usr/local/bin"});
        }
        return null;
    }

    @Unique
    private static Path vansqmod$searchDirs(String fileName, String[] dirs) {
        for (String dir : dirs) {
            if (dir == null || dir.isEmpty()) {
                continue;
            }
            try {
                Path candidate = Paths.get(dir).resolve(fileName);
                if (Files.isRegularFile(candidate) && Files.isExecutable(candidate)) {
                    return candidate.toAbsolutePath();
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}

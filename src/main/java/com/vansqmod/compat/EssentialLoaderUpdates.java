package com.vansqmod.compat;

import com.vansqmod.VansqMod;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Essential's Skip/Update window is a loader Swing dialog, not the in-game modal.
 * Stage2 shows it before vansqmod mixins exist if {@code autoUpdate=with-prompt}
 * and {@code pendingUpdateVersion} has no {@code pendingUpdateResolution}.
 * Cancelling the in-game modal without writing a skip is what triggers that fallback.
 */
public final class EssentialLoaderUpdates {

    private EssentialLoaderUpdates() {
    }

    public static void suppressPrompts() {
        Path essentialDir = FMLPaths.GAMEDIR.get().resolve("essential");
        if (!Files.isDirectory(essentialDir)) {
            return;
        }
        writeSkip(essentialDir.resolve("essential-loader.properties"));
    }

    private static void writeSkip(Path file) {
        Properties properties = new Properties();
        if (Files.isRegularFile(file)) {
            try (InputStream in = Files.newInputStream(file)) {
                properties.load(in);
            } catch (IOException e) {
                VansqMod.LOGGER.debug("Could not read {}", file, e);
                return;
            }
        }
        String autoUpdate = properties.getProperty("autoUpdate");
        String resolution = properties.getProperty("pendingUpdateResolution");
        boolean changed = false;
        if (!"false".equals(autoUpdate)) {
            properties.setProperty("autoUpdate", "false");
            changed = true;
        }
        if (properties.getProperty("pendingUpdateVersion") != null && !"false".equals(resolution)) {
            properties.setProperty("pendingUpdateResolution", "false");
            changed = true;
        }
        if (!changed) {
            return;
        }
        try {
            Files.createDirectories(file.getParent());
            try (OutputStream out = Files.newOutputStream(file)) {
                properties.store(out, null);
            }
        } catch (IOException e) {
            VansqMod.LOGGER.debug("Could not write {}", file, e);
        }
    }
}

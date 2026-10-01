package com.vansqmod.compat;

import com.vansqmod.VansqMod;

import javax.imageio.ImageIO;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Copies a PNG screenshot to the OS clipboard in-process.
 * Essential's {@code ForkedImageClipboard} builds a forked-JVM classpath via nested Jar-in-Jar URLs
 * (e.g. KotlinLangForge {@code #647_} paths) that do not exist on disk, so copy never completes.
 */
public final class EssentialScreenshotClipboard {

    private EssentialScreenshotClipboard() {
    }

    public static boolean copyPng(Path screenshot) {
        if (screenshot == null) {
            return false;
        }
        File file = screenshot.toFile();
        if (!file.isFile()) {
            VansqMod.LOGGER.warn("Screenshot clipboard: file missing {}", screenshot);
            return false;
        }
        if (copyViaEssentialClipboard(file)) {
            return true;
        }
        if (!GraphicsEnvironment.isHeadless() && copyViaAwt(file)) {
            return true;
        }
        if (isWindows() && copyViaPowerShell(file)) {
            return true;
        }
        VansqMod.LOGGER.warn("Screenshot clipboard: all copy methods failed for {}", screenshot);
        return false;
    }

    public static void notifyCopied() {
        try {
            Class.forName("gg.essential.gui.NotificationsKt")
                    .getMethod("sendPictureCopiedNotification")
                    .invoke(null);
        } catch (ReflectiveOperationException ignored) {
            // Clipboard copy still succeeded; Essential toast is best-effort.
        }
    }

    public static void notifyFailed() {
        try {
            Class<?> notifications = Class.forName("gg.essential.gui.notification.Notifications");
            Object instance = notifications.getField("INSTANCE").get(null);
            notifications.getMethod("push", String.class, String.class)
                    .invoke(instance, "Failed to copy picture", "");
        } catch (ReflectiveOperationException ignored) {
            VansqMod.LOGGER.warn("Screenshot clipboard copy failed and Essential error toast could not be shown");
        }
    }

    private static boolean copyViaEssentialClipboard(File file) {
        try {
            Class<?> clipboard = Class.forName("gg.essential.clipboard.Clipboard");
            Object instance = clipboard.getMethod("current").invoke(null);
            Object result = clipboard.getMethod("copyPNG", File.class).invoke(instance, file);
            return Boolean.TRUE.equals(result);
        } catch (Throwable t) {
            VansqMod.LOGGER.debug("Essential Clipboard.copyPNG failed, trying fallback", t);
            return false;
        }
    }

    private static boolean copyViaAwt(File file) {
        try {
            BufferedImage image = ImageIO.read(file);
            if (image == null) {
                return false;
            }
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new ImageTransferable(image), null);
            return true;
        } catch (Throwable t) {
            VansqMod.LOGGER.debug("AWT screenshot clipboard copy failed, trying fallback", t);
            return false;
        }
    }

    private static boolean copyViaPowerShell(File file) {
        Path script = null;
        try {
            script = Files.createTempFile("vansq-clipboard", ".ps1");
            String path = file.getAbsolutePath().replace("'", "''");
            String body = "Add-Type -AssemblyName System.Windows.Forms\r\n"
                    + "Add-Type -AssemblyName System.Drawing\r\n"
                    + "$img = [System.Drawing.Image]::FromFile('" + path + "')\r\n"
                    + "[System.Windows.Forms.Clipboard]::SetImage($img)\r\n"
                    + "$img.Dispose()\r\n";
            Files.writeString(script, body, StandardCharsets.UTF_8);
            Process process = new ProcessBuilder(
                    "powershell.exe",
                    "-STA",
                    "-NoProfile",
                    "-NonInteractive",
                    "-ExecutionPolicy", "Bypass",
                    "-File", script.toAbsolutePath().toString()
            ).redirectErrorStream(true).start();
            boolean finished = process.waitFor(20, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return false;
            }
            return process.exitValue() == 0;
        } catch (Exception e) {
            VansqMod.LOGGER.debug("PowerShell screenshot clipboard copy failed", e);
            return false;
        } finally {
            if (script != null) {
                try {
                    Files.deleteIfExists(script);
                } catch (Exception ignored) {
                }
            }
        }
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
    }

    private record ImageTransferable(Image image) implements Transferable {
        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{DataFlavor.imageFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.imageFlavor.equals(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return image;
        }
    }
}

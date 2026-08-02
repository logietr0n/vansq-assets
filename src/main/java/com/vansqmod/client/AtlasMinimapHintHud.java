package com.vansqmod.client;

import com.vansqmod.VansqMod;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import pepjebs.mapatlases.client.Anchoring;
import pepjebs.mapatlases.client.MapAtlasesClient;
import pepjebs.mapatlases.config.MapAtlasesClientConfig;

import java.util.List;

/**
 * When Xaero's Minimap / World Map are installed (and blocked by vansqmod), shows a
 * centered Atlas hint in the minimap slot. Visible for 2 minutes after joining,
 * then fades out over 10 seconds. Resets on rejoin.
 */
@EventBusSubscriber(modid = VansqMod.MODID, value = Dist.CLIENT)
public final class AtlasMinimapHintHud implements LayeredDraw.Layer {

    private static final int BG_SIZE = 64;
    private static final int PADDING = 4;
    /** Full hint opacity before fade (50%). */
    private static final float BASE_ALPHA = 0.5F;
    private static final long VISIBLE_MS = 2L * 60L * 1000L;
    private static final long FADE_MS = 10L * 1000L;
    private static final Component HINT = Component.literal(
            "Craft or find an Atlas in a firewatch tower to unlock the minimap");

    /** Wall-clock start of the current client world session, or {@code -1} when none. */
    private static long sessionStartMs = -1L;

    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        sessionStartMs = Util.getMillis();
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        sessionStartMs = -1L;
    }

    private static boolean xaeroPresent() {
        return ModList.get().isLoaded("xaerominimap") || ModList.get().isLoaded("xaeroworldmap");
    }

    @Override
    public void render(GuiGraphics graphics, net.minecraft.client.DeltaTracker deltaTracker) {
        if (!xaeroPresent()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || ClientHudVisibility.shouldHideHudOverlays()) {
            return;
        }
        if (mc.getDebugOverlay().showDebugScreen()) {
            return;
        }
        if (!MapAtlasesClientConfig.drawMiniMapHUD.get()) {
            return;
        }
        if (MapAtlasesClientConfig.hideWhenInventoryOpen.get() && mc.screen != null) {
            return;
        }

        ItemStack activeAtlas = MapAtlasesClient.getCurrentActiveAtlas();
        if (activeAtlas != null && !activeAtlas.isEmpty()) {
            return;
        }

        float alpha = currentAlpha();
        if (alpha <= 0.0F) {
            return;
        }

        float globalScale = MapAtlasesClientConfig.miniMapScale.get().floatValue();
        float textScaling = MapAtlasesClientConfig.minimapCoordsAndBiomeScale.get().floatValue();
        Anchoring anchor = MapAtlasesClientConfig.miniMapAnchoring.get();

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int[] pos = computeMapPosition(mc, screenWidth, screenHeight, anchor, globalScale);
        int mapX = pos[0];
        int mapY = pos[1];

        Font font = mc.font;
        // Same effective scale as the Day counter (see MapAtlasesInstrumentHud.drawTime).
        float scale = textScaling / globalScale;
        int wrapWidth = Math.max(8, (int) ((BG_SIZE - PADDING * 2) / Math.max(1.0E-4F, scale)));

        List<FormattedCharSequence> lines = font.split(HINT, wrapWidth);
        if (lines.isEmpty()) {
            return;
        }

        int lineHeight = font.lineHeight;
        float blockHeight = lines.size() * lineHeight * scale;
        float blockWidth = 0.0F;
        for (FormattedCharSequence line : lines) {
            blockWidth = Math.max(blockWidth, font.width(line) * scale);
        }

        float originX = mapX + (BG_SIZE - blockWidth) * 0.5F;
        float originY = mapY + (BG_SIZE - blockHeight) * 0.5F;
        int textColor = ((int) (alpha * 255.0F) << 24) | 0xFFFFFF;

        graphics.pose().pushPose();
        graphics.pose().scale(globalScale, globalScale, 1.0F);
        if (!anchor.isUp) {
            graphics.pose().translate(0.0F, -BG_SIZE - 20.0F * textScaling - 2.0F, 0.0F);
        }

        graphics.pose().pushPose();
        graphics.pose().translate(originX, originY, 0.0F);
        graphics.pose().scale(scale, scale, 1.0F);

        int y = 0;
        for (FormattedCharSequence line : lines) {
            int lineW = font.width(line);
            int x = (int) ((blockWidth / scale - lineW) * 0.5F);
            graphics.drawString(font, line, x, y, textColor, false);
            y += lineHeight;
        }

        graphics.pose().popPose();
        graphics.pose().popPose();
    }

    private static float currentAlpha() {
        if (sessionStartMs < 0L) {
            // Joined before this class loaded events; start the timer on first draw.
            sessionStartMs = Util.getMillis();
        }
        long elapsed = Util.getMillis() - sessionStartMs;
        if (elapsed < VISIBLE_MS) {
            return BASE_ALPHA;
        }
        long fadeElapsed = elapsed - VISIBLE_MS;
        if (fadeElapsed >= FADE_MS) {
            return 0.0F;
        }
        float fadeT = fadeElapsed / (float) FADE_MS;
        return BASE_ALPHA * (1.0F - Mth.clamp(fadeT, 0.0F, 1.0F));
    }

    private static int[] computeMapPosition(
            Minecraft mc,
            int screenWidth,
            int screenHeight,
            Anchoring anchorLocation,
            float globalScale
    ) {
        int off = 5;
        int x = anchorLocation.isLeft ? off : (int) (screenWidth / globalScale) - (BG_SIZE + off);
        int y = anchorLocation.isUp ? off : (int) (screenHeight / globalScale) - (BG_SIZE + off);
        x += (int) (MapAtlasesClientConfig.miniMapHorizontalOffset.get() / globalScale);
        y += (int) (MapAtlasesClientConfig.miniMapVerticalOffset.get() / globalScale);

        if (anchorLocation == Anchoring.UPPER_RIGHT && mc.player != null) {
            boolean hasBeneficial = false;
            boolean hasNegative = false;
            for (var e : mc.player.getActiveEffects()) {
                Holder<MobEffect> effect = e.getEffect();
                if (effect.value().isBeneficial()) {
                    hasBeneficial = true;
                } else {
                    hasNegative = true;
                }
            }
            int offsetForEffects = MapAtlasesClientConfig.activePotionVerticalOffset.get();
            if (hasNegative && y < 2 * offsetForEffects) {
                y += (2 * offsetForEffects - y);
            } else if (hasBeneficial && y < offsetForEffects) {
                y += (offsetForEffects - y);
            }
        }
        return new int[]{x, y};
    }
}

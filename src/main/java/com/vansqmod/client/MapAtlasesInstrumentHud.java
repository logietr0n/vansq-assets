package com.vansqmod.client;

import com.vansqmod.compat.MapAtlasesHudInstruments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import pepjebs.mapatlases.client.Anchoring;
import pepjebs.mapatlases.client.MapAtlasesClient;
import pepjebs.mapatlases.client.ui.MapAtlasesHUD;
import pepjebs.mapatlases.config.MapAtlasesClientConfig;

/**
 * Draws instrument HUD text (coords / time) when Map Atlases would otherwise skip
 * its entire overlay because no atlas is currently active.
 */
public final class MapAtlasesInstrumentHud implements LayeredDraw.Layer {

    private static final int BG_SIZE = 64;
    private static final int TIME_Y_NUDGE = 3;

    @Override
    public void render(GuiGraphics graphics, net.minecraft.client.DeltaTracker deltaTracker) {
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

        // Atlas HUD is rendering; the mixin gates its text. Avoid doubling.
        ItemStack activeAtlas = MapAtlasesClient.getCurrentActiveAtlas();
        if (activeAtlas != null && !activeAtlas.isEmpty()) {
            return;
        }

        LocalPlayer player = mc.player;
        if (!MapAtlasesHudInstruments.showAny(player)) {
            return;
        }

        boolean showXZ = MapAtlasesHudInstruments.showHorizontalCoords(player);
        boolean showY = MapAtlasesHudInstruments.showAltitude(player);
        boolean showTime = MapAtlasesHudInstruments.showTime(player);

        float globalScale = MapAtlasesClientConfig.miniMapScale.get().floatValue();
        float textScaling = MapAtlasesClientConfig.minimapCoordsAndBiomeScale.get().floatValue();
        Anchoring anchor = MapAtlasesClientConfig.miniMapAnchoring.get();

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int[] pos = computeMapPosition(mc, screenWidth, screenHeight, anchor, globalScale);
        int x = pos[0];
        int y = pos[1];

        graphics.pose().pushPose();
        graphics.pose().scale(globalScale, globalScale, 1.0F);
        if (!anchor.isUp) {
            graphics.pose().translate(0.0F, -BG_SIZE - 20.0F * textScaling - 2.0F, 0.0F);
        }

        Font font = mc.font;
        int actualBgSize = (int) (BG_SIZE * globalScale);
        int textY = (int) (y + BG_SIZE + (2.0F / globalScale));
        int line = Math.max(1, Math.round(10.0F * textScaling));

        BlockPos blockPos = new BlockPos(new Vec3i(
                towardsZero(player.getX()),
                towardsZero(player.getY()),
                towardsZero(player.getZ())
        ));

        if (showXZ || showY) {
            String coords;
            if (showXZ && showY) {
                coords = Component.translatable(
                        "message.map_atlases.coordinates_full",
                        blockPos.getX(), blockPos.getY(), blockPos.getZ()
                ).getString();
            } else if (showXZ) {
                coords = Component.translatable(
                        "message.map_atlases.coordinates",
                        blockPos.getX(), blockPos.getZ()
                ).getString();
            } else {
                coords = Integer.toString(blockPos.getY());
            }
            drawScaled(graphics, font, x, textY, coords, textScaling, actualBgSize, globalScale);
            textY += line - TIME_Y_NUDGE;
        }

        if (showTime) {
            drawTime(
                    graphics, font, x, textY,
                    MapAtlasesHudInstruments.formatDayAndTime(player.level()),
                    textScaling, actualBgSize, globalScale
            );
        }

        graphics.pose().popPose();
    }

    public static void drawScaled(
            GuiGraphics context,
            Font font,
            int x,
            int y,
            String text,
            float textScaling,
            int targetWidth,
            float globalScale
    ) {
        MapAtlasesHUD.drawScaledComponent(
                context,
                font,
                x,
                y,
                text,
                textScaling / globalScale,
                targetWidth,
                (int) (targetWidth / globalScale)
        );
    }

    public static void drawTime(
            GuiGraphics context,
            Font font,
            int x,
            int y,
            String text,
            float textScaling,
            int targetWidth,
            float globalScale
    ) {
        float scale = textScaling / globalScale;
        int centerWidth = (int) (targetWidth / globalScale);
        int maxWidth = (int) Math.ceil(font.width(text) / Math.max(1.0E-4F, scale)) + 8;
        MapAtlasesHUD.drawScaledComponent(
                context,
                font,
                x,
                y,
                text,
                scale,
                maxWidth,
                centerWidth
        );
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

    private static int towardsZero(double d) {
        return d < 0.0 ? -1 * (int) Math.floor(-d) : (int) Math.floor(d);
    }
}

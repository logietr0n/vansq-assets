package com.vansqmod.mixin.mapatlases;

import com.vansqmod.compat.MapAtlasesHudInstruments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.vansqmod.client.MapAtlasesInstrumentHud;
import pepjebs.mapatlases.client.ui.MapAtlasesHUD;

/**
 * Gates Map Atlases HUD coordinates / time by held / Curios instruments:
 * held/curios compass → X/Z, depth gauge → Y, clock → day/time,
 * atlas → all.
 */
@Mixin(value = MapAtlasesHUD.class, remap = false)
public abstract class MapAtlasesHudCoordsMixin {

    @Shadow
    private float globalScale;

    /**
     * Pixel delta applied to the atlas HUD text stack after the global-coords call.
     * Atlas always advances one line; we add {@code (linesDrawn - 1) * lineHeight}.
     */
    @Unique
    private static final ThreadLocal<Integer> HEIGHT_DELTA = ThreadLocal.withInitial(() -> 0);

    /** Extra Y shift for biome text when time is drawn inside {@code drawMapComponentBiome}. */
    @Unique
    private static final ThreadLocal<Integer> BIOME_TEXT_OFFSET = ThreadLocal.withInitial(() -> 0);

    @Unique
    private static final ThreadLocal<Boolean> TIME_DRAWN = ThreadLocal.withInitial(() -> false);

    /** Pulls the time line a few pixels closer to the coordinates row. */
    @Unique
    private static final int TIME_Y_NUDGE = 3;

    @Inject(method = "drawMapComponentCoords", at = @At("HEAD"), cancellable = true)
    private void vansqmod$gateCoordsAndDrawTime(
            GuiGraphics context,
            Font font,
            int x,
            int y,
            int targetWidth,
            float textScaling,
            BlockPos pos,
            boolean chunk,
            CallbackInfo ci
    ) {
        if (chunk) {
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || player.level() == null) {
            return;
        }

        boolean showXZ = MapAtlasesHudInstruments.showHorizontalCoords(player);
        boolean showY = MapAtlasesHudInstruments.showAltitude(player);
        boolean showTime = MapAtlasesHudInstruments.showTime(player);

        int line = Math.max(1, Math.round(10.0F * textScaling));
        int nextY = y;
        int linesDrawn = 0;

        if (showXZ || showY) {
            String coords;
            if (showXZ && showY) {
                coords = Component.translatable(
                        "message.map_atlases.coordinates_full",
                        pos.getX(), pos.getY(), pos.getZ()
                ).getString();
            } else if (showXZ) {
                coords = Component.translatable(
                        "message.map_atlases.coordinates",
                        pos.getX(), pos.getZ()
                ).getString();
            } else {
                coords = Integer.toString(pos.getY());
            }
            MapAtlasesInstrumentHud.drawScaled(
                    context, font, x, nextY, coords, textScaling, targetWidth, globalScale);
            nextY += line - TIME_Y_NUDGE;
            linesDrawn++;
        }

        if (showTime) {
            MapAtlasesInstrumentHud.drawTime(
                    context, font, x, nextY,
                    MapAtlasesHudInstruments.formatDayAndTime(player.level()),
                    textScaling, targetWidth, globalScale
            );
            linesDrawn++;
            TIME_DRAWN.set(true);
        }

        HEIGHT_DELTA.set((linesDrawn - 1) * line - (showTime && linesDrawn > 1 ? TIME_Y_NUDGE : 0));
        ci.cancel();
    }

    @ModifyVariable(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lpepjebs/mapatlases/client/ui/MapAtlasesHUD;drawMapComponentCoords(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;IIIFLnet/minecraft/core/BlockPos;Z)V",
                    shift = At.Shift.AFTER,
                    ordinal = 0
            ),
            index = 20
    )
    private int vansqmod$adjustTextStackAfterGlobalCoords(int textHeightOffset) {
        return textHeightOffset + HEIGHT_DELTA.get();
    }

    @Inject(method = "drawMapComponentBiome", at = @At("HEAD"))
    private void vansqmod$drawTimeBeforeBiome(
            GuiGraphics context,
            Font font,
            int x,
            int y,
            int targetWidth,
            float textScaling,
            BlockPos blockPos,
            Level level,
            CallbackInfo ci
    ) {
        if (TIME_DRAWN.get()) {
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !MapAtlasesHudInstruments.showTime(player)) {
            return;
        }
        MapAtlasesInstrumentHud.drawTime(
                context, font, x, y - TIME_Y_NUDGE,
                MapAtlasesHudInstruments.formatDayAndTime(player.level()),
                textScaling, targetWidth, globalScale
        );
        int line = Math.max(1, Math.round(10.0F * textScaling));
        BIOME_TEXT_OFFSET.set(line - TIME_Y_NUDGE);
        TIME_DRAWN.set(true);
    }

    @ModifyArg(
            method = "drawMapComponentBiome",
            at = @At(
                    value = "INVOKE",
                    target = "Lpepjebs/mapatlases/client/ui/MapAtlasesHUD;drawScaledComponent(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;IILjava/lang/String;FII)V"
            ),
            index = 3
    )
    private int vansqmod$offsetBiomeTextForTime(int y) {
        return y + BIOME_TEXT_OFFSET.get();
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void vansqmod$clearHudTextState(CallbackInfo ci) {
        HEIGHT_DELTA.remove();
        BIOME_TEXT_OFFSET.remove();
        TIME_DRAWN.remove();
    }
}

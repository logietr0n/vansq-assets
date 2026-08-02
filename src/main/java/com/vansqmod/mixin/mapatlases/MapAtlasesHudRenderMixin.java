package com.vansqmod.mixin.mapatlases;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vansqmod.client.ClientHudVisibility;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pepjebs.mapatlases.client.ui.MapAtlasesHUD;

/**
 * Fixes Map Atlases minimap: hide with F1 / Better F1, and draw the player marker above the map.
 */
@Mixin(value = MapAtlasesHUD.class, remap = false)
public abstract class MapAtlasesHudRenderMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void vansqmod$hideWhenGuiHidden(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (ClientHudVisibility.shouldHideHudOverlays()) {
            ci.cancel();
        }
    }

    /** Map vertices write depth; keep the following marker/text from failing the depth test. */
    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    // Invoke owner is MapAtlasesHUD in the 6.4.0 jar (inherited method).
                    target = "Lpepjebs/mapatlases/client/ui/MapAtlasesHUD;drawAtlas(Lnet/minecraft/client/gui/GuiGraphics;IIIILnet/minecraft/world/entity/player/Player;FZLpepjebs/mapatlases/utils/MapType;ILnet/minecraft/world/level/saveddata/maps/MapItemSavedData;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void vansqmod$disableDepthAfterMap(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        RenderSystem.disableDepthTest();
    }

    /** Push the player arrow well in front of map geometry (map uses inverted Z scale). */
    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V"
            ),
            slice = @Slice(
                    from = @At(
                            value = "FIELD",
                            opcode = Opcodes.GETSTATIC,
                            target = "Lpepjebs/mapatlases/client/MapAtlasesClient;PLAYER_MARKER_SPRITE:Lnet/minecraft/resources/ResourceLocation;"
                    )
            )
    )
    private void vansqmod$playerMarkerOnTop(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        RenderSystem.disableDepthTest();
        graphics.pose().translate(0.0F, 0.0F, 400.0F);
    }
}

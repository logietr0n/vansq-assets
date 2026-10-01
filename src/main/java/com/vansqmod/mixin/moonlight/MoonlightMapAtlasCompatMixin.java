package com.vansqmod.mixin.moonlight;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pepjebs.mapatlases.client.MapAtlasesClient;
import pepjebs.mapatlases.item.MapAtlasItem;
import pepjebs.mapatlases.map_collection.MapCollection;
import pepjebs.mapatlases.utils.MapDataHolder;
import pepjebs.mapatlases.utils.Slice;

/**
 * Moonlight 3.7 still calls Map Atlases APIs that 6.4 removed. {@code MapAtlasesApi}
 * is replaced for atlas right-click, and {@code MapAtlasesClientApi.scaleDecoration}
 * is replaced so player markers on the atlas HUD do not crash the client.
 */
@Mixin(targets = "net.mehvahdjukaar.moonlight.core.integration.MapAtlasCompat", remap = false)
public abstract class MoonlightMapAtlasCompatMixin {

    @Inject(method = "getSavedDataFromAtlas", at = @At("HEAD"), cancellable = true, remap = false)
    private static void vansqmod$useMapSearchKey(
            ItemStack stack,
            Level level,
            Player player,
            CallbackInfoReturnable<MapItemSavedData> cir
    ) {
        MapCollection maps = MapAtlasItem.getMaps(stack, level);
        if (maps == null) {
            cir.setReturnValue(null);
            return;
        }
        Slice slice = MapAtlasItem.getSelectedSlice(stack, level.dimension());
        MapDataHolder holder = maps.getClosest(player, slice);
        cir.setReturnValue(holder != null ? holder.data : null);
    }

    @Inject(method = "scaleDecoration", at = @At("HEAD"), cancellable = true, remap = false)
    private static void vansqmod$scaleDecoration(PoseStack pose, CallbackInfo ci) {
        MapAtlasesClient.modifyDecorationTransform(pose);
        ci.cancel();
    }

    @Inject(method = "scaleDecorationText", at = @At("HEAD"), cancellable = true, remap = false)
    private static void vansqmod$scaleDecorationText(PoseStack pose, float x, float y, CallbackInfo ci) {
        MapAtlasesClient.modifyTextDecorationTransform(pose, x, y);
        ci.cancel();
    }
}

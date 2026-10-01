package com.vansqmod.mixin.astronomy;

import com.vansqmod.client.AstronomyClientSync;
import com.vansqmod.client.AstronomySpyglassControls;
import com.vansqmod.client.SpyglassZooming;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient", remap = false)
public abstract class AstronomySkyMixin {

    @ModifyConstant(method = "<clinit>", constant = @Constant(intValue = 1024))
    private static int vansqmod$defaultStarCount(int original) {
        return AstronomyClientSync.DEFAULT_STAR_COUNT;
    }

    @Inject(method = "generateStars", at = @At("HEAD"))
    private static void vansqmod$migrateOldDefaultStarCount(CallbackInfo ci) {
        AstronomyClientSync.ensureDoubledDefaultStarCount();
    }

    @ModifyConstant(method = "generateStars", constant = @Constant(floatValue = 0.15F))
    private static float vansqmod$halfMinStarSize(float original) {
        return 0.075F;
    }

    @ModifyConstant(method = "generateStars", constant = @Constant(floatValue = 0.2F, ordinal = 1))
    private static float vansqmod$keepMaxStarSize(float original) {
        return 0.275F;
    }

    @Inject(method = "toggleEditMode", at = @At("HEAD"), cancellable = true)
    private static void vansqmod$keepNormalZoomMode(CallbackInfo ci) {
        ci.cancel();
    }

    @ModifyVariable(method = "update", at = @At(value = "STORE", ordinal = 0), index = 0)
    private static boolean vansqmod$spyglassIncludesCurioZoom(boolean spyglassing) {
        return spyglassing || SpyglassZooming.isImprovementsZoom();
    }

    @ModifyConstant(method = "update", constant = @Constant(intValue = 1))
    private static int vansqmod$drawConstellationsInZoom(int original) {
        return 0;
    }

    @ModifyConstant(method = "update", constant = @Constant(intValue = 2))
    private static int vansqmod$selectObjectsInZoom(int original) {
        return 0;
    }

    @Redirect(
            method = "update",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/nettakrim/spyglass_astronomy/SpyglassAstronomyClient;spyglassImprovementsIsLoaded:Z",
                    opcode = Opcodes.GETSTATIC
            )
    )
    private static boolean vansqmod$alwaysFollowDrawingCursor() {
        return true;
    }

    @Inject(method = "startDrawingConstellation", at = @At("HEAD"), cancellable = true)
    private static void vansqmod$drawStarsNotPlanets(CallbackInfo ci) {
        if (AstronomySpyglassControls.nearestIsOrbitingBody()) {
            ci.cancel();
        }
    }

    @Inject(method = "selectAstralObject", at = @At("HEAD"), cancellable = true)
    private static void vansqmod$selectPlanetsNotStars(CallbackInfo ci) {
        if (AstronomySpyglassControls.isDrawing() || !AstronomySpyglassControls.nearestIsOrbitingBody()) {
            ci.cancel();
        }
    }

    @Inject(method = "updateHover", at = @At("HEAD"), cancellable = true)
    private static void vansqmod$hoverWithoutStarNames(CallbackInfo ci) {
        AstronomySpyglassControls.updateHover();
        ci.cancel();
    }
}

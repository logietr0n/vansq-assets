package com.vansqmod.mixin.subtleeffects;

import com.teamremastered.endrem.item.JsonEye;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Subtle Effects 1.14.3 calls {@code JsonEye.getID()} as if it returned {@link ResourceLocation},
 * but End Remastered 6.3.0 exposes {@code getID(): String}. That binary mismatch crashes Subtle
 * Effects during client config construction ({@code NoSuchMethodError}).
 * <p>
 * Rebuild the eye ID list with the String API, matching Subtle Effects'
 * {@code CompatHelper.endRemLoc(path)} ({@code endrem:<path>}).
 */
@Mixin(targets = "einstein.subtle_effects.compat.EndRemasteredCompat", remap = false)
public abstract class SubtleEffectsEndRemasteredCompatMixin {

    @Inject(method = "getAllEyes", at = @At("HEAD"), cancellable = true, remap = false)
    private static void vansqmod$bridgeJsonEyeIds(CallbackInfoReturnable<List<ResourceLocation>> cir) {
        try {
            ArrayList<JsonEye> eyes = JsonEye.getEyes();
            if (eyes == null || eyes.isEmpty()) {
                cir.setReturnValue(List.of());
                return;
            }

            List<ResourceLocation> ids = new ArrayList<>(eyes.size());
            for (JsonEye eye : eyes) {
                String id = eye.getID();
                if (id == null || id.isEmpty()) {
                    continue;
                }
                String path = id.indexOf(':') >= 0
                        ? ResourceLocation.parse(id).getPath()
                        : id;
                ids.add(ResourceLocation.fromNamespaceAndPath("endrem", path));
            }
            cir.setReturnValue(Collections.unmodifiableList(ids));
        } catch (Throwable ignored) {
            // Keep Subtle Effects loading even if End Remastered eyes aren't ready yet.
            cir.setReturnValue(List.of());
        }
    }
}

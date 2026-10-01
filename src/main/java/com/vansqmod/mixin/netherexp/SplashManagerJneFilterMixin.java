package com.vansqmod.mixin.netherexp;

import net.minecraft.client.resources.SplashManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * JNE's {@code enableJNESplashTexts = false} can fail to stick (startup-config timing during
 * splash reload). Strip any {@code [JNE]}-prefixed lines after SplashManager finishes applying.
 */
@Mixin(value = SplashManager.class, priority = 2000)
public class SplashManagerJneFilterMixin {

    private static final String JNE_PREFIX = "[JNE]";

    @Shadow
    @Final
    private List<String> splashes;

    @Inject(method = "apply(Ljava/util/List;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("RETURN"))
    private void vansqmod$stripJneSplashes(
            List<String> list,
            ResourceManager resourceManager,
            ProfilerFiller profiler,
            CallbackInfo ci
    ) {
        this.splashes.removeIf(s -> s != null && s.startsWith(JNE_PREFIX));
    }
}

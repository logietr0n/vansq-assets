package com.vansqmod.mixin;

import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.client.color.block.BlockColors;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ModelBakery.class)
public abstract class ModelBakeryMixin {

    @Shadow
    protected abstract void loadSpecialItemModelAndDependencies(ModelResourceLocation modelResourceLocation);

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", ordinal = 1))
    private void vansqmod$loadRoseGoldSpearModels(BlockColors blockColors, ProfilerFiller profilerFiller, Map<?, ?> map, Map<?, ?> map2, CallbackInfo ci) {
        this.loadSpecialItemModelAndDependencies(
                ModelResourceLocation.inventory(ResourceLocation.withDefaultNamespace("rose_gold_spear_in_hand")));
        this.loadSpecialItemModelAndDependencies(
                ModelResourceLocation.inventory(ResourceLocation.withDefaultNamespace("silver_spear_in_hand")));
        this.loadSpecialItemModelAndDependencies(
                ModelResourceLocation.inventory(ResourceLocation.withDefaultNamespace("necromium_spear_in_hand")));
        this.loadSpecialItemModelAndDependencies(
                ModelResourceLocation.inventory(ResourceLocation.fromNamespaceAndPath("vansqmod", "scythe_in_hand")));
    }
}

package com.vansqmod.mixin.copperage;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorMaterial;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Workaround for copperagebackport #101 / NeoForge 21.1.237+.
 * <p>
 * {@code CopperArmorMaterial.COPPER} is a supplier that calls {@code createCopper()} on every
 * {@code get()}. Item registration invokes it once per copper armor piece, so the second call
 * tries to re-register {@code minecraft:copper} and crashes. NeoForge 21.1.237 tightened
 * duplicate-key rejection (PR #3302); older NeoForge silently tolerated this.
 * <p>
 * Removable once Copper Age Backport caches the holder in {@code init()}.
 */
@Mixin(targets = "com.github.smallinger.copperagebackport.item.armor.CopperArmorMaterial", remap = false)
public abstract class CopperArmorMaterialMixin {

    private static final ResourceLocation COPPER_MATERIAL_ID =
            ResourceLocation.withDefaultNamespace("copper");

    @Inject(method = "createCopper", at = @At("HEAD"), cancellable = true)
    private static void vansqmod$reuseRegisteredCopper(CallbackInfoReturnable<Holder<ArmorMaterial>> cir) {
        BuiltInRegistries.ARMOR_MATERIAL.getHolder(COPPER_MATERIAL_ID).ifPresent(holder -> {
            if (holder.isBound()) {
                cir.setReturnValue(holder);
            }
        });
    }
}

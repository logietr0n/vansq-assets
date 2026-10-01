package com.vansqmod.mixin.yungscavebiomes;

import com.vansqmod.worldgen.ArchaeologyLootSeeds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * YUNG copied vanilla brushable-block logic into its own entity. Template NBT
 * still carries two fixed loot seeds, so every Lost Caves well rolled the same
 * top and bottom items. Reseed from world position whenever the table is loaded.
 */
@Mixin(
        targets = "com.yungnickyoung.minecraft.yungscavebiomes.block.entity.SuspiciousAncientSandBlockEntity",
        remap = false
)
public abstract class SuspiciousAncientSandLootMixin {

    @Inject(
            method = "loadAdditional(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;)V",
            at = @At("RETURN"),
            remap = true
    )
    private void vansqmod$uniqueArchaeologySeed(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries, CallbackInfo ci) {
        String threadName = Thread.currentThread().getName();
        if (threadName.startsWith("DH-")
                || threadName.contains("DistantHorizons")
                || threadName.contains("LOD World Gen")) {
            return;
        }
        try {
            ArchaeologyLootSeeds.applyFromNbt((BlockEntity) (Object) this, tag);
        } catch (NoClassDefFoundError ignored) {
        }
    }
}

package com.vansqmod.mixin;

import com.vansqmod.worldgen.ArchaeologyLootSeeds;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Vanilla only rewrites {@code LootTableSeed} for chests. Brushable blocks keep
 * whatever seed was saved in the structure file.
 *
 * Distant Horizons FEATURES generation cannot load vansqmod classes on its worker
 * threads; skip there so LOD gen is not stalled by {@link NoClassDefFoundError}.
 */
@Mixin(StructureTemplate.class)
public abstract class StructureTemplateArchaeologyLootMixin {

    @Redirect(
            method = "placeInWorld",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/entity/BlockEntity;loadWithComponents(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;)V"
            )
    )
    private void vansqmod$uniqueArchaeologySeed(BlockEntity blockEntity, CompoundTag tag, HolderLookup.Provider registries) {
        String threadName = Thread.currentThread().getName();
        if (threadName.startsWith("DH-")
                || threadName.contains("DistantHorizons")
                || threadName.contains("LOD World Gen")) {
            blockEntity.loadWithComponents(tag, registries);
            return;
        }
        try {
            blockEntity.loadWithComponents(ArchaeologyLootSeeds.withUniqueSeed(blockEntity, tag), registries);
        } catch (NoClassDefFoundError ignored) {
            blockEntity.loadWithComponents(tag, registries);
        }
    }
}

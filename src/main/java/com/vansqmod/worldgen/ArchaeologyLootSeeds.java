package com.vansqmod.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.storage.loot.LootTable;

import java.lang.reflect.Method;

/**
 * Vanilla structure placement randomizes chest loot seeds but copies brushable-block
 * seeds from the template NBT. YUNG's suspicious ancient sand is a separate block
 * entity, so wells kept the two seeds baked into {@code vansq:lost_caves_well}.
 */
public final class ArchaeologyLootSeeds {

    private static final long SALT = 71829446L;
    private static final String LOOT_TABLE_TAG = "LootTable";
    private static final String LOOT_TABLE_SEED_TAG = "LootTableSeed";
    private static final String ITEM_TAG = "item";

    private ArchaeologyLootSeeds() {
    }

    public static void applyFromNbt(BlockEntity blockEntity, CompoundTag tag) {
        if (blockEntity == null || tag == null || !tag.contains(LOOT_TABLE_TAG)) {
            return;
        }
        ResourceLocation tableId = ResourceLocation.tryParse(tag.getString(LOOT_TABLE_TAG));
        if (tableId == null) {
            return;
        }
        apply(blockEntity, ResourceKey.create(Registries.LOOT_TABLE, tableId));
    }

    public static void apply(BlockEntity blockEntity, ResourceKey<LootTable> table) {
        if (blockEntity == null || table == null) {
            return;
        }
        long seed = seedFor(blockEntity);
        if (blockEntity instanceof BrushableBlockEntity brushable) {
            brushable.setLootTable(table, seed);
            return;
        }
        try {
            Method method = blockEntity.getClass().getMethod("setLootTable", ResourceKey.class, long.class);
            method.invoke(blockEntity, table, seed);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    public static CompoundTag withUniqueSeed(BlockEntity blockEntity, CompoundTag tag) {
        if (blockEntity == null || tag == null || !tag.contains(LOOT_TABLE_TAG)) {
            return tag;
        }
        CompoundTag copy = tag.copy();
        copy.putLong(LOOT_TABLE_SEED_TAG, seedFor(blockEntity));
        copy.remove(ITEM_TAG);
        return copy;
    }

    public static long seedFor(BlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        long worldSeed = 0L;
        if (blockEntity.getLevel() instanceof ServerLevel serverLevel) {
            worldSeed = serverLevel.getSeed();
        }
        long seed = worldSeed ^ BlockPos.asLong(pos.getX(), pos.getY(), pos.getZ()) ^ SALT;
        return seed == 0L ? 1L : seed;
    }
}

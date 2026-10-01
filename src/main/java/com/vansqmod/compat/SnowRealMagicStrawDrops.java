package com.vansqmod.compat;

import com.vansqmod.VansqMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

import java.lang.reflect.Method;

/**
 * Snow Real Magic replaces grass/fern with {@code snowy_plant} wrappers. Farmer's Delight's
 * straw loot modifiers key off the vanilla block ids, so snowy plants never drop straw.
 * Read the contained plant from the snow block entity and roll the same chances as
 * {@code vansqtweaks} (0.4 grass/fern/dry grass, 0.6 bush).
 */
@EventBusSubscriber(modid = VansqMod.MODID)
public final class SnowRealMagicStrawDrops {

    private static final ResourceLocation STRAW_ID =
            ResourceLocation.fromNamespaceAndPath("farmersdelight", "straw");
    private static final TagKey<Item> STRAW_HARVESTERS = TagKey.create(
            Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath("farmersdelight", "straw_harvesters")
    );

    private SnowRealMagicStrawDrops() {
    }

    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        if (!ModList.get().isLoaded("snowrealmagic") || !ModList.get().isLoaded("farmersdelight")) {
            return;
        }
        Level level = event.getLevel() instanceof Level l ? l : null;
        if (level == null || level.isClientSide()) {
            return;
        }
        ItemStack tool = event.getTool();
        if (tool.isEmpty() || !tool.is(STRAW_HARVESTERS)) {
            return;
        }
        BlockState contained = containedPlant(event.getBlockEntity());
        if (contained == null) {
            return;
        }
        float chance = strawChance(contained);
        if (chance <= 0.0f || level.random.nextFloat() >= chance) {
            return;
        }
        Item straw = BuiltInRegistries.ITEM.get(STRAW_ID);
        if (straw == null || straw == net.minecraft.world.item.Items.AIR) {
            return;
        }
        ItemStack drop = TweedDrops.maybeReplace(new ItemStack(straw), level.random);
        var pos = event.getPos();
        event.getDrops().add(new ItemEntity(
                level,
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                drop
        ));
    }

    private static BlockState containedPlant(BlockEntity blockEntity) {
        if (blockEntity == null) {
            return null;
        }
        String name = blockEntity.getClass().getName();
        if (!name.startsWith("snownee.snow.block.entity.")) {
            return null;
        }
        try {
            Method method = blockEntity.getClass().getMethod("getContainedState");
            Object value = method.invoke(blockEntity);
            return value instanceof BlockState state && !state.isAir() ? state : null;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static float strawChance(BlockState state) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (id == null || !"minecraft".equals(id.getNamespace())) {
            return 0.0f;
        }
        return switch (id.getPath()) {
            case "short_grass", "tall_grass", "fern", "large_fern",
                 "short_dry_grass", "tall_dry_grass" -> 0.4f;
            case "bush" -> 0.6f;
            default -> 0.0f;
        };
    }
}

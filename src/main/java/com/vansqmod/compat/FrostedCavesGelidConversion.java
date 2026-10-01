package com.vansqmod.compat;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.biome.Biome;

import javax.annotation.Nullable;

/**
 * Frosted Caves force {@code isInPowderSnow} on skeletons so vanilla freeze-converts
 * them to strays. Zombies have no freeze conversion, so we mirror the same 7s + 15s
 * shake and turn vanilla zombies into Gelids.
 */
public final class FrostedCavesGelidConversion {

    public static final ResourceKey<Biome> FROSTED_CAVES = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath("yungscavebiomes", "frosted_caves"));

    public static final ResourceLocation GELID_ID =
            ResourceLocation.fromNamespaceAndPath("variantsandventures", "gelid");

    /** Same as {@code Skeleton.inPowderSnowTime} before shaking starts. */
    public static final int PRE_SHAKE_TICKS = 140;
    /** Same as {@code Skeleton.TOTAL_CONVERSION_TIME}. */
    public static final int SHAKE_TICKS = 300;
    /** Vanilla stray conversion sound. */
    public static final int CONVERSION_LEVEL_EVENT = 1048;

    public static final String CONVERSION_TIME_TAG = "vansqmod:GelidConversionTime";

    private FrostedCavesGelidConversion() {
    }

    public static boolean isVanillaZombie(Zombie zombie) {
        return zombie.getType() == EntityType.ZOMBIE;
    }

    public static boolean inFrostedCaves(Zombie zombie) {
        return zombie.level().getBiome(zombie.blockPosition()).is(FROSTED_CAVES);
    }

    @Nullable
    public static EntityType<?> gelidType() {
        return BuiltInRegistries.ENTITY_TYPE.getOptional(GELID_ID).orElse(null);
    }

    @SuppressWarnings("unchecked")
    public static boolean convert(Zombie zombie) {
        EntityType<?> type = gelidType();
        if (type == null) {
            return false;
        }
        if (zombie.convertTo((EntityType<? extends Mob>) type, true) == null) {
            return false;
        }
        if (!zombie.isSilent()) {
            zombie.level().levelEvent(null, CONVERSION_LEVEL_EVENT, zombie.blockPosition(), 0);
        }
        return true;
    }

    public interface Access {
        boolean vansqmod$isGelidConverting();
    }
}

package com.vansqmod.registry;

import com.vansqmod.VansqMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public final class ModEntityTags {

    /**
     * Entity types that should not be treated as hostile monsters by features
     * (iron golems, sleep checks, and similar), while keeping spawn category and AI.
     */
    public static final TagKey<EntityType<?>> NOT_MONSTERS = TagKey.create(
            Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "not_monsters")
    );

    /**
     * Entity types that roll {@code vansqmod:entities/antidote_vessel}
     * (0.5% + 0.25% per Looting). Add IDs here to give a mob the same drop.
     */
    public static final TagKey<EntityType<?>> DROPS_ANTIDOTE_VESSEL = TagKey.create(
            Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "drops_antidote_vessel")
    );

    /**
     * Entity types vansqmod shows a boss health bar for. Keep in sync with
     * {@link com.vansqmod.boss.BossBarRegistry}.
     */
    public static final TagKey<EntityType<?>> BOSS_HEALTH_BARS = TagKey.create(
            Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "boss_health_bars")
    );

    private ModEntityTags() {
    }
}

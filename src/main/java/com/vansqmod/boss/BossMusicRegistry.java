package com.vansqmod.boss;

import com.vansqmod.VansqMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Per-entity-type intro/loop music. Add more bosses here using the same format.
 */
public final class BossMusicRegistry {

    public static final ResourceLocation WITHER = ResourceLocation.withDefaultNamespace("wither");
    public static final ResourceLocation VOID_WORM =
            ResourceLocation.fromNamespaceAndPath("alexsmobs", "void_worm");

    private static final Map<ResourceLocation, BossMusicSpec> TRACKS = Map.ofEntries(
            Map.entry(WITHER, BossMusicSpec.untilEntityStateEnds(
                    ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "music.boss.wither.intro"),
                    ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "music.boss.wither.loop"),
                    60,
                    200,
                    living -> living instanceof WitherBoss wither && wither.getInvulnerableTicks() > 0
            )),
            Map.entry(BossBarRegistry.MISSIONER, BossMusicSpec.onFightStart(
                    ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "music.boss.missionary.intro"),
                    ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "music.boss.missionary.loop"),
                    60,
                    200,
                    BossFightState::isFightActive
            )),
            Map.entry(BossBarRegistry.MUTANT_ENDERMAN, BossMusicSpec.onFightStart(
                    ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "music.boss.enderman.intro"),
                    ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "music.boss.enderman.loop"),
                    60,
                    200,
                    BossFightState::isFightActive
            )),
            Map.entry(BossBarRegistry.BERSERKER, BossMusicSpec.onFightStart(
                    ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "music.boss.berserker.intro"),
                    ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "music.boss.berserker.loop"),
                    60,
                    200,
                    BossFightState::isFightActive
            )),
            Map.entry(VOID_WORM, BossMusicSpec.loopWithDeathOutro(
                    ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "music.boss.void_worm.loop"),
                    ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "music.boss.void_worm.outro"),
                    0,
                    200,
                    living -> true
            ))
    );

    private BossMusicRegistry() {
    }

    public static @Nullable BossMusicSpec get(Entity entity) {
        if (entity == null) {
            return null;
        }
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return id == null ? null : TRACKS.get(id);
    }
}

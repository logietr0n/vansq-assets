package com.vansqmod.mixin.spideroverhaul;

import com.vansqmod.entity.IceSpiderSpawns;
import com.vansqmod.entity.OceanSpiderSpawns;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Vanilla {@code NaturalSpawner} already applies the monster cap, per-chunk
 * attempts, and biome spawn weights. Spider Overhaul only needs to be skipped
 * on the {@link SpawnPlacements#checkSpawnRules} call (Y&lt;63). Redirect that
 * invoke so ocean/ice spiders use our predicates and never enter that method.
 */
@Mixin(NaturalSpawner.class)
public abstract class NaturalSpawnerSpiderOverhaulMixin {

    @Redirect(
            method = "isValidSpawnPostitionForType",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/SpawnPlacements;checkSpawnRules(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/entity/MobSpawnType;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)Z"
            )
    )
    private static boolean vansqmod$overhaulProofSpawnRules(
            EntityType<?> type,
            ServerLevelAccessor level,
            MobSpawnType reason,
            BlockPos pos,
            RandomSource random
    ) {
        if (OceanSpiderSpawns.isOceanSpider(type)) {
            @SuppressWarnings("unchecked")
            EntityType<Mob> oceanSpider = (EntityType<Mob>) type;
            return OceanSpiderSpawns.canSpawn(oceanSpider, level, reason, pos, random);
        }
        if (IceSpiderSpawns.isIceSpider(type)) {
            @SuppressWarnings("unchecked")
            EntityType<Monster> iceSpider = (EntityType<Monster>) type;
            return IceSpiderSpawns.canSpawn(iceSpider, level, reason, pos, random);
        }
        return SpawnPlacements.checkSpawnRules(type, level, reason, pos, random);
    }
}

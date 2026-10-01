package com.vansqmod.mixin.spideroverhaul;

import com.vansqmod.entity.IceSpiderSpawns;
import com.vansqmod.entity.OceanSpiderSpawns;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Spider Overhaul rejects variant spiders at Y&lt;63 (and can refuse vanilla
 * spiders that cannot see the sky) with a RETURN inject on
 * {@link SpawnPlacements#checkSpawnRules}. Mixin-on-mixin is forbidden, so this
 * later RETURN inject (priority {@code 10000}, order {@code 10000}) overwrites
 * that result for other call sites. Natural spawning skips this method entirely
 * via {@link NaturalSpawnerSpiderOverhaulMixin}. Cavern spiders stay refused.
 */
@Mixin(value = SpawnPlacements.class, priority = 10000)
public abstract class SpiderSpawnRestrictionMixin {

    @Inject(method = "checkSpawnRules", at = @At("RETURN"), cancellable = true, order = 10000)
    private static void vansqmod$undoOverhaulYRules(
            EntityType<?> type,
            ServerLevelAccessor level,
            MobSpawnType reason,
            BlockPos pos,
            RandomSource random,
            CallbackInfoReturnable<Boolean> cir
    ) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        if (id != null && "spider_overhaul".equals(id.getNamespace())) {
            if ("cavern_spider".equals(id.getPath())) {
                cir.setReturnValue(false);
                return;
            }
            if ("ocean_spider".equals(id.getPath())) {
                @SuppressWarnings("unchecked")
                EntityType<Mob> oceanSpider = (EntityType<Mob>) type;
                cir.setReturnValue(OceanSpiderSpawns.canSpawn(oceanSpider, level, reason, pos, random));
                return;
            }
            if ("ice_spider".equals(id.getPath())) {
                @SuppressWarnings("unchecked")
                EntityType<Monster> iceSpider = (EntityType<Monster>) type;
                cir.setReturnValue(IceSpiderSpawns.canSpawn(iceSpider, level, reason, pos, random));
                return;
            }
            @SuppressWarnings("unchecked")
            EntityType<? extends Monster> monsterType = (EntityType<? extends Monster>) type;
            cir.setReturnValue(Monster.checkMonsterSpawnRules(monsterType, level, reason, pos, random));
            return;
        }
        if (type == EntityType.SPIDER || type == EntityType.CAVE_SPIDER) {
            @SuppressWarnings("unchecked")
            EntityType<? extends Monster> spider = (EntityType<? extends Monster>) type;
            cir.setReturnValue(Monster.checkMonsterSpawnRules(spider, level, reason, pos, random));
        }
    }
}

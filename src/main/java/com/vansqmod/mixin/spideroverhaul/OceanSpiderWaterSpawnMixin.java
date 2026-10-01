package com.vansqmod.mixin.spideroverhaul;

import com.vansqmod.entity.OceanSpiderSpawns;
import dev.chybx.spideroverhaul.entity.AbstractVariantSpiderEntity;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Spider crabs are still {@link net.minecraft.world.entity.monster.Spider}s.
 * {@link net.minecraft.world.entity.Mob#checkSpawnObstruction} rejects liquid,
 * and {@link net.minecraft.world.entity.monster.Monster#checkSpawnRules} would
 * re-apply ground-monster rules. Match drowned: ignore liquid, keep collision,
 * and reuse {@link OceanSpiderSpawns#canSpawn} for natural attempts.
 */
@Mixin(targets = "dev.chybx.spideroverhaul.entity.OceanSpiderEntity")
public abstract class OceanSpiderWaterSpawnMixin extends AbstractVariantSpiderEntity {

    protected OceanSpiderWaterSpawnMixin(EntityType<? extends Spider> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType reason) {
        if (reason != MobSpawnType.NATURAL && reason != MobSpawnType.CHUNK_GENERATION) {
            return level.getDifficulty() != Difficulty.PEACEFUL;
        }
        if (!(level instanceof ServerLevelAccessor server)) {
            return false;
        }
        @SuppressWarnings("unchecked")
        EntityType<Mob> type = (EntityType<Mob>) this.getType();
        return OceanSpiderSpawns.canSpawn(type, server, reason, this.blockPosition(), this.getRandom());
    }

    @Override
    public boolean checkSpawnObstruction(LevelReader level) {
        return level.isUnobstructed(this);
    }
}

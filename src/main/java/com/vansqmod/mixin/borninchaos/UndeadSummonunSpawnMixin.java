package com.vansqmod.mixin.borninchaos;

import com.vansqmod.compat.MissionerAttackSummons;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Born in Chaos applies {@code undead_summonun} when the Missionary aggroes a player; when that
 * effect expires it hardcodes nine entity types. Replace those {@code EntityType.spawn} calls
 * with {@link MissionerAttackSummons}.
 */
@Mixin(
        targets = "net.mcreator.borninchaosv.procedures.UndeadSummonunKazhdyiTikVoVriemiaEffiektaProcedure",
        remap = false
)
public abstract class UndeadSummonunSpawnMixin {

    private static final String EXECUTE =
            "execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V";

    @Inject(method = EXECUTE, at = @At("HEAD"), remap = false)
    private static void vansqmod$beginMissionerSummon(
            LevelAccessor level,
            double x,
            double y,
            double z,
            Entity entity,
            CallbackInfo ci
    ) {
        MissionerAttackSummons.beginAttack(x, y, z);
    }

    @Inject(method = EXECUTE, at = @At("RETURN"), remap = false)
    private static void vansqmod$endMissionerSummon(
            LevelAccessor level,
            double x,
            double y,
            double z,
            Entity entity,
            CallbackInfo ci
    ) {
        MissionerAttackSummons.endAttack();
    }

    @Redirect(
            method = EXECUTE,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/EntityType;spawn(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/MobSpawnType;)Lnet/minecraft/world/entity/Entity;",
                    remap = true
            ),
            remap = false
    )
    private static Entity vansqmod$replaceMissionerSummon(
            EntityType<?> type,
            ServerLevel level,
            BlockPos pos,
            MobSpawnType spawnType
    ) {
        return MissionerAttackSummons.spawnReplacement(type, level, pos, spawnType);
    }
}

package com.vansqmod.mixin.borninchaos;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Jaw Attack spawned four Evoker Fangs around the target (up to 24 magic damage).
 * Keep a single fang at the target's feet (vanilla 6 magic).
 */
@Mixin(
        targets = "net.mcreator.borninchaosv.procedures.JawattackPriNalozhieniiEffiektaProcedure",
        remap = false
)
public abstract class JawattackFangsMixin {

    private static final String EXECUTE =
            "execute(Lnet/minecraft/world/level/LevelAccessor;DDD)V";

    @Unique
    private static final ThreadLocal<Boolean> VANSQMOD$SPAWNED = ThreadLocal.withInitial(() -> Boolean.FALSE);

    @Inject(method = EXECUTE, at = @At("HEAD"), remap = false)
    private static void vansqmod$beginFangSpawn(
            LevelAccessor level,
            double x,
            double y,
            double z,
            CallbackInfo ci
    ) {
        VANSQMOD$SPAWNED.set(Boolean.FALSE);
    }

    @Inject(method = EXECUTE, at = @At("RETURN"), remap = false)
    private static void vansqmod$endFangSpawn(
            LevelAccessor level,
            double x,
            double y,
            double z,
            CallbackInfo ci
    ) {
        VANSQMOD$SPAWNED.remove();
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
    private static Entity vansqmod$oneFang(
            EntityType<?> type,
            ServerLevel level,
            BlockPos pos,
            MobSpawnType spawnType
    ) {
        if (Boolean.TRUE.equals(VANSQMOD$SPAWNED.get())) {
            return null;
        }
        VANSQMOD$SPAWNED.set(Boolean.TRUE);
        return type.spawn(level, pos, spawnType);
    }
}

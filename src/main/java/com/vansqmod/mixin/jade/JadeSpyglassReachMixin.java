package com.vansqmod.mixin.jade;

import com.vansqmod.client.SpyglassZooming;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import snownee.jade.overlay.RayTracing;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * While a spyglass is zoomed (vanilla use or Spyglass Improvements), Jade traces
 * 64 blocks so distant blocks and mobs still show name and health.
 *
 * <p>Jade's entity hit uses {@code entity.position()} (feet/origin) instead of the
 * ray clip, so a block behind the mob can win the distance check. Use the clip
 * point so the looked-at entity stays selected.
 */
@Mixin(RayTracing.class)
public abstract class JadeSpyglassReachMixin {

    private static final double SPYGLASS_REACH = 64.0;
    private static final double SMALL_ENTITY_INFLATE = 0.3;

    @ModifyVariable(method = "rayTrace", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private double vansqmod$spyglassBlockReach(double blockReach) {
        return spyglassReach(blockReach);
    }

    @ModifyVariable(method = "rayTrace", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private double vansqmod$spyglassEntityReach(double entityReach) {
        return spyglassReach(entityReach);
    }

    @Inject(method = "getEntityHitResult", at = @At("HEAD"), cancellable = true)
    private static void vansqmod$entityHitUsesClipPoint(
            Level level,
            Entity source,
            Vec3 start,
            Vec3 end,
            AABB searchBox,
            Predicate<Entity> filter,
            CallbackInfoReturnable<EntityHitResult> cir
    ) {
        double closest = Double.MAX_VALUE;
        Entity picked = null;
        Vec3 hitPos = null;
        List<Entity> entities = level.getEntities(source, searchBox, filter);
        for (Entity entity : entities) {
            AABB box = entity.getBoundingBox();
            if (box.getSize() < SMALL_ENTITY_INFLATE) {
                box = box.inflate(SMALL_ENTITY_INFLATE);
            }
            if (box.contains(start)) {
                picked = entity;
                hitPos = start;
                break;
            }
            Optional<Vec3> clip = box.clip(start, end);
            if (clip.isPresent()) {
                double dist = start.distanceToSqr(clip.get());
                if (dist < closest) {
                    closest = dist;
                    picked = entity;
                    hitPos = clip.get();
                }
            }
        }
        cir.setReturnValue(picked == null ? null : new EntityHitResult(picked, hitPos));
    }

    private static double spyglassReach(double current) {
        return SpyglassZooming.isZooming() ? Math.max(current, SPYGLASS_REACH) : current;
    }
}

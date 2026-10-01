package com.vansqmod.mixin;

import com.vansqmod.debug.VansqDebugState;
import com.vansqmod.entity.SkeletonBabies;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Right-clicking a skeleton (or skeleton variant) with its own spawn egg spawns that mob's baby.
 * Vanilla drops the offspring when {@code isBaby()} is still false after {@code setBaby(true)}.
 */
@Mixin(SpawnEggItem.class)
public class SpawnEggSkeletonBabyMixin {

    @Inject(method = "spawnOffspringFromSpawnEgg", at = @At("HEAD"), cancellable = true)
    private void vansqmod$spawnSkeletonBaby(
            Player player,
            Mob target,
            EntityType<? extends Mob> entityType,
            ServerLevel level,
            Vec3 pos,
            ItemStack stack,
            CallbackInfoReturnable<Optional<Mob>> cir
    ) {
        if (!SkeletonBabies.isFamily(target) || !((SpawnEggItem) (Object) this).spawnsEntity(stack, entityType)) {
            return;
        }
        Mob baby = entityType.create(level);
        if (baby == null) {
            return;
        }
        SkeletonBabies.markAsBaby(baby);
        SkeletonBabies.prepareBaby(baby);
        if (!SkeletonBabies.isMarkedBaby(baby)) {
            baby.discard();
            return;
        }
        baby.moveTo(pos.x(), pos.y(), pos.z(), target.getYRot(), 0.0F);
        SkeletonBabies.markBabyRollDone(baby);
        level.addFreshEntityWithPassengers(baby);
        if (VansqDebugState.rareEventSucceeds(baby.getRandom(), SkeletonBabies.NATURAL_CHANCE)) {
            SkeletonBabies.spawnNearbyBaby(baby);
        }
        baby.setCustomName(stack.get(DataComponents.CUSTOM_NAME));
        stack.consume(1, player);
        cir.setReturnValue(Optional.of(baby));
    }
}

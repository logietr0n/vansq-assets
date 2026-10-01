package com.vansqmod.mixin.spideroverhaul;

import com.vansqmod.entity.IceSnowballProjectile;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "dev.chybx.spideroverhaul.item.IceSnowballItem")
public abstract class IceSnowballItemMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void vansqmod$throwCustomProjectile(
            Level level,
            Player player,
            InteractionHand hand,
            CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir
    ) {
        ItemStack stack = player.getItemInHand(hand);
        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.SNOWBALL_THROW,
                SoundSource.NEUTRAL,
                0.5F,
                0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
        );
        if (!level.isClientSide) {
            IceSnowballProjectile projectile = new IceSnowballProjectile(
                    level,
                    player,
                    stack.copyWithCount(1)
            );
            projectile.shootFromRotation(
                    player,
                    player.getXRot(),
                    player.getYRot(),
                    0.0F,
                    IceSnowballProjectile.THROW_SPEED,
                    IceSnowballProjectile.THROW_INACCURACY
            );
            level.addFreshEntity(projectile);
        }
        player.awardStat(Stats.ITEM_USED.get((Item) (Object) this));
        if (!player.isCreative()) {
            stack.shrink(1);
        }
        cir.setReturnValue(InteractionResultHolder.sidedSuccess(stack, level.isClientSide()));
    }
}

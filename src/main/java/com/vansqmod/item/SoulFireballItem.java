package com.vansqmod.item;

import com.vansqmod.entity.SoulFireballProjectile;
import com.vansqmod.registry.ModItems;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@EventBusSubscriber(modid = com.vansqmod.VansqMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class SoulFireballItem extends Item implements ProjectileItem {

    public SoulFireballItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.FIRECHARGE_USE,
                SoundSource.NEUTRAL,
                1.0F,
                (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2F + 1.0F
        );
        player.getCooldowns().addCooldown(this, SoulFireballProjectile.THROW_COOLDOWN);
        if (!level.isClientSide) {
            SoulFireballProjectile projectile = new SoulFireballProjectile(
                    level,
                    player,
                    stack.copyWithCount(1)
            );
            projectile.shootFromRotation(
                    player,
                    player.getXRot(),
                    player.getYRot(),
                    0.0F,
                    SoulFireballProjectile.THROW_SPEED,
                    1.0F
            );
            level.addFreshEntity(projectile);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        stack.consume(1, player);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        SoulFireballProjectile projectile = new SoulFireballProjectile(
                level,
                pos.x(),
                pos.y(),
                pos.z(),
                stack.copyWithCount(1),
                null
        );
        projectile.shoot(direction.getStepX(), direction.getStepY(), direction.getStepZ(), SoulFireballProjectile.THROW_SPEED, 1.0F);
        return projectile;
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> DispenserBlock.registerProjectileBehavior(ModItems.SOUL_FIRE_CHARGE.get()));
    }
}

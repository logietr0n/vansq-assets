package com.vansqmod.compat;

import com.vansqmod.VansqMod;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Born in Chaos corpse/fried maggots eat on the click instead of a chew, with a hidden
 * 0.1s gap so holding use cannot dump a stack in a few ticks.
 */
@EventBusSubscriber(modid = VansqMod.MODID)
public final class BornInChaosMaggotEatHandler {

    private static final Set<ResourceLocation> MAGGOTS = Set.of(
            ResourceLocation.fromNamespaceAndPath("born_in_chaos_v1", "corpse_maggot"),
            ResourceLocation.fromNamespaceAndPath("born_in_chaos_v1", "fried_maggot"),
            ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "golden_maggot")
    );

    /** 0.1s at 20 tps. Not applied through {@code ItemCooldowns}, so the hotbar overlay stays off. */
    private static final int COOLDOWN_TICKS = 2;
    /** One vanilla chew burst ({@code LivingEntity.spawnItemParticles} count). */
    private static final int EAT_PARTICLES = 5;

    private static final Map<UUID, Long> LAST_EAT_CLIENT = new HashMap<>();
    private static final Map<UUID, Long> LAST_EAT_SERVER = new HashMap<>();

    private BornInChaosMaggotEatHandler() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        ItemStack stack = event.getItemStack();
        if (!isMaggot(stack)) {
            return;
        }
        Player player = event.getEntity();
        if (isOnCooldown(player)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
            return;
        }
        FoodProperties food = stack.getFoodProperties(player);
        if (food == null || !player.canEat(food.canAlwaysEat())) {
            return;
        }
        markCooldown(player);
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.CONSUME);
        if (player.level().isClientSide()) {
            // Packet is already on the way. Consume and effects run on the server so
            // client prediction cannot block the eat or drop the chew/burp sounds.
            return;
        }
        InteractionHand hand = event.getHand();
        ItemStack crumbs = stack.copyWithCount(1);
        ItemStack result = stack.finishUsingItem(player.level(), player);
        if (player.getItemInHand(hand) != result) {
            player.setItemInHand(hand, result);
        }
        spawnEatingParticles(player, crumbs);
        playChewSound(player, crumbs);
    }

    private static boolean isMaggot(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return MAGGOTS.contains(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    private static Map<UUID, Long> lastEatTimes(Player player) {
        return player.level().isClientSide() ? LAST_EAT_CLIENT : LAST_EAT_SERVER;
    }

    private static boolean isOnCooldown(Player player) {
        Long last = lastEatTimes(player).get(player.getUUID());
        return last != null && player.level().getGameTime() - last < COOLDOWN_TICKS;
    }

    private static void markCooldown(Player player) {
        lastEatTimes(player).put(player.getUUID(), player.level().getGameTime());
    }

    /**
     * Same mouth-offset crumbs as {@code LivingEntity.spawnItemParticles}. Sent from the server
     * so nearby players see them once (client {@code addParticle} would double in singleplayer).
     */
    private static void spawnEatingParticles(Player player, ItemStack eaten) {
        if (!(player.level() instanceof ServerLevel serverLevel) || eaten.isEmpty()) {
            return;
        }
        RandomSource random = player.getRandom();
        ItemParticleOption particle = new ItemParticleOption(ParticleTypes.ITEM, eaten);
        for (int i = 0; i < EAT_PARTICLES; i++) {
            Vec3 vel = new Vec3((random.nextFloat() - 0.5) * 0.1, Math.random() * 0.1 + 0.1, 0.0);
            vel = vel.xRot(-player.getXRot() * (float) (Math.PI / 180.0));
            vel = vel.yRot(-player.getYRot() * (float) (Math.PI / 180.0));
            double drop = -random.nextFloat() * 0.6 - 0.3;
            Vec3 pos = new Vec3((random.nextFloat() - 0.5) * 0.3, drop, 0.6);
            pos = pos.xRot(-player.getXRot() * (float) (Math.PI / 180.0));
            pos = pos.yRot(-player.getYRot() * (float) (Math.PI / 180.0));
            pos = pos.add(player.getX(), player.getEyeY(), player.getZ());
            serverLevel.sendParticles(particle, pos.x, pos.y, pos.z, 0, vel.x, vel.y + 0.05, vel.z, 1.0);
        }
    }

    /** Chew from {@code LivingEntity.triggerItemUseEffects}; burp still comes from {@code Player.eat}. */
    private static void playChewSound(Player player, ItemStack eaten) {
        if (!(player.level() instanceof ServerLevel serverLevel) || eaten.isEmpty()) {
            return;
        }
        RandomSource random = player.getRandom();
        serverLevel.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                player.getEatingSound(eaten),
                SoundSource.PLAYERS,
                0.5F + 0.5F * random.nextInt(2),
                (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F
        );
    }
}

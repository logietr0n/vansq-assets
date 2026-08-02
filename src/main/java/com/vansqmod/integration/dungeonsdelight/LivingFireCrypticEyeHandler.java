package com.vansqmod.integration.dungeonsdelight;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.yirmiri.dungeonsdelight.common.block.entity.LivingFireBlockEntity;
import net.neoforged.fml.ModList;

import java.util.Optional;

/**
 * Transforms a thrown vanilla ender eye into an End Remastered cryptic eye when it enters living fire
 * charged with at least nine levels of stored experience, consuming that charge on success.
 */
public final class LivingFireCrypticEyeHandler {

    private static final int REQUIRED_LEVELS = 9;
    private static final int REQUIRED_EXPERIENCE = totalExperienceForLevel(REQUIRED_LEVELS);
    private static final ResourceLocation LIVING_FIRE = ResourceLocation.fromNamespaceAndPath("dungeonsdelight", "living_fire");
    private static final ResourceLocation CRYPTIC_EYE = ResourceLocation.fromNamespaceAndPath("endrem", "cryptic_eye");
    private static final ResourceLocation MONSTERIZE_ACTIVATE = ResourceLocation.fromNamespaceAndPath("dungeonsdelight", "effect.monsterize.activate");

    private LivingFireCrypticEyeHandler() {
    }

    public static boolean isEnabled() {
        return ModList.get().isLoaded("dungeonsdelight") && ModList.get().isLoaded("endrem");
    }

    public static void tryTransformEnderEye(ItemEntity entity) {
        if (!isEnabled() || entity.level().isClientSide() || !entity.isAlive()) {
            return;
        }

        ItemStack stack = entity.getItem();
        if (stack.isEmpty() || !stack.is(Items.ENDER_EYE)) {
            return;
        }

        Level level = entity.level();
        BlockPos firePos = findLivingFireAt(entity);
        if (firePos == null) {
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(firePos);
        if (!(blockEntity instanceof LivingFireBlockEntity livingFire)
                || livingFire.getStoredExperience() < REQUIRED_EXPERIENCE) {
            return;
        }

        Optional<Item> crypticEye = level.registryAccess()
                .registryOrThrow(net.minecraft.core.registries.Registries.ITEM)
                .getOptional(CRYPTIC_EYE);
        if (crypticEye.isEmpty()) {
            return;
        }

        livingFire.setStoredExperience(livingFire.getStoredExperience() - REQUIRED_EXPERIENCE);
        livingFire.setChanged();

        ItemStack crypticStack = new ItemStack(crypticEye.get());
        if (stack.getCount() <= 1) {
            entity.setItem(crypticStack);
        } else {
            stack.shrink(1);
            entity.setItem(stack);
            spawnCrypticEye(level, entity, crypticStack);
        }

        playMonsterizeSound(level, entity);
    }

    private static BlockPos findLivingFireAt(ItemEntity entity) {
        Level level = entity.level();
        Optional<Block> livingFireBlock = level.registryAccess()
                .registryOrThrow(net.minecraft.core.registries.Registries.BLOCK)
                .getOptional(LIVING_FIRE);
        if (livingFireBlock.isEmpty()) {
            return null;
        }

        Block livingFire = livingFireBlock.get();
        AABB bounds = entity.getBoundingBox().inflate(0.05D);
        BlockPos min = BlockPos.containing(bounds.minX, bounds.minY, bounds.minZ);
        BlockPos max = BlockPos.containing(bounds.maxX, bounds.maxY, bounds.maxZ);

        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            BlockState state = level.getBlockState(pos);
            if (state.is(livingFire)) {
                return pos.immutable();
            }
        }
        return null;
    }

    private static void spawnCrypticEye(Level level, ItemEntity source, ItemStack crypticStack) {
        ItemEntity spawned = new ItemEntity(level, source.getX(), source.getY(), source.getZ(), crypticStack);
        spawned.setDeltaMovement(source.getDeltaMovement());
        spawned.setDefaultPickUpDelay();
        level.addFreshEntity(spawned);
    }

    private static void playMonsterizeSound(Level level, ItemEntity entity) {
        Optional<SoundEvent> sound = level.registryAccess()
                .registryOrThrow(net.minecraft.core.registries.Registries.SOUND_EVENT)
                .getOptional(MONSTERIZE_ACTIVATE);
        sound.ifPresent(event -> level.playSound(
                null,
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                event,
                SoundSource.PLAYERS,
                1.0F,
                1.0F
        ));
    }

    private static int experienceNeededForLevel(int level) {
        if (level >= 30) {
            return 112 + (level - 30) * 9;
        }
        if (level >= 15) {
            return 37 + (level - 15) * 5;
        }
        return 7 + level * 2;
    }

    private static int totalExperienceForLevel(int level) {
        int total = 0;
        for (int i = 0; i < level; i++) {
            total += experienceNeededForLevel(i);
        }
        return total;
    }
}

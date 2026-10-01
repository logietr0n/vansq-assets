package com.vansqmod.integration.backpacks;

import com.spydnel.backpacks.common.items.BackpackItem;
import com.spydnel.backpacks.registry.BPBlocks;
import com.spydnel.backpacks.registry.BPSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Place/pickup for backpacks on Curios {@code back}.
 */
public final class BackpackPickupHandler {

    private BackpackPickupHandler() {
    }

    /**
     * Places the equipped Curios backpack or picks up a looked-at backpack block.
     * Invoked from the place/pickup keybind on the server.
     *
     * @return true if an action was performed
     */
    public static boolean tryPlaceOrPickup(Player player) {
        if (player == null || player.level().isClientSide() || player.isSpectator()) {
            return false;
        }

        Level level = player.level();
        BlockHitResult hit = clipLook(player);
        if (hit.getType() != HitResult.Type.BLOCK) {
            return false;
        }

        BlockPos pos = hit.getBlockPos();
        Block block = level.getBlockState(pos).getBlock();
        BlockEntity blockEntity = level.getBlockEntity(pos);
        ItemStack backItem = BackpackEquipment.getEquippedBackpack(player).orElse(ItemStack.EMPTY);
        boolean hasBackpack = BackpackEquipment.isBackpack(backItem);
        boolean backSlotEmpty = backItem.isEmpty();

        // Pick up placed backpack into Curios back (chestplate may still occupy chest).
        if (backSlotEmpty && block == BPBlocks.BACKPACK.get() && blockEntity != null) {
            ItemStack itemstack = new ItemStack(BPBlocks.BACKPACK);
            itemstack.applyComponents(blockEntity.collectComponents());
            BackpackEquipment.setEquippedBackpack(player, itemstack);
            playEquipSound(level, player);
            addParticles(level, pos);
            level.removeBlockEntity(pos);
            level.removeBlock(pos, false);
            return true;
        }

        if (hasBackpack && hit.getDirection() == Direction.UP) {
            if (!level.getBlockState(pos).useWithoutItem(level, player, hit).consumesAction()) {
                BlockPlaceContext context = new BlockPlaceContext(player, InteractionHand.MAIN_HAND, backItem, hit);
                InteractionResult result = ((BackpackItem) backItem.getItem()).place(context);
                if (result.consumesAction()) {
                    BackpackEquipment.setEquippedBackpack(player, ItemStack.EMPTY);
                    // BlockItem.place already broadcasts the place sound to everyone except the placer.
                    // Keybind has no client prediction, so notify the placer only.
                    player.playNotifySound(
                            BPSounds.BACKPACK_PLACE.value(),
                            SoundSource.BLOCKS,
                            1.0F,
                            1.0F
                    );
                    return true;
                }
            }
            return false;
        }

        return false;
    }

    private static void playEquipSound(Level level, Player player) {
        level.playSound(
                null,
                player.blockPosition(),
                BPSounds.BACKPACK_EQUIP.value(),
                SoundSource.PLAYERS,
                1.0F,
                1.1F
        );
    }

    private static BlockHitResult clipLook(Player player) {
        double reach = player.blockInteractionRange();
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.calculateViewVector(player.getXRot(), player.getYRot()).scale(reach));
        return player.level().clip(new ClipContext(
                eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
    }

    private static void addParticles(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        for (int i = 0; i < 4; i++) {
            serverLevel.sendParticles(
                    ParticleTypes.DUST_PLUME,
                    pos.getX() + 0.5,
                    pos.getY(),
                    pos.getZ() + 0.5,
                    1,
                    0.0,
                    0.0,
                    0.0,
                    0.0
            );
        }
    }
}

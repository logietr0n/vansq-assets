package com.vansqmod.compat;

import net.mehvahdjukaar.sleep_tight.STPlatStuff;
import net.mehvahdjukaar.sleep_tight.common.entities.BedEntity;
import net.mehvahdjukaar.sleep_tight.common.items.BedbugEggsItem;
import net.mehvahdjukaar.sleep_tight.core.BedData;
import net.mehvahdjukaar.sleep_tight.core.PlayerSleepData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

/**
 * Sleep Tight lays one player across two matching beds. When someone else
 * uses one of those beds, the first player keeps the bed that was not clicked
 * and lies in it alone.
 */
public final class SleepTightDoubleBedSplit {

    private SleepTightDoubleBedSplit() {
    }

    public static void onBedClick(Player clicker, Level level, InteractionHand hand, BlockHitResult hit) {
        if (clicker.isSpectator() || clicker.isSecondaryUseActive() || !BedBlock.canSetSpawn(level)) {
            return;
        }
        if (clicker.getItemInHand(hand).getItem() instanceof BedbugEggsItem) {
            return;
        }

        BlockPos clicked = hit.getBlockPos();
        BlockState clickedState = level.getBlockState(clicked);
        if (!isBed(clickedState)) {
            return;
        }

        BedData bedData = STPlatStuff.getBedDataIfPresent(level, clicked);
        if (bedData == null || bedData.isInfested()) {
            return;
        }

        BlockPos head = bedHead(clickedState, clicked);
        BlockState headState = level.getBlockState(head);
        if (!isBedHead(headState) || !headState.getValue(BedBlock.OCCUPIED)) {
            return;
        }
        if (bedBlocked(level, head, headState.getValue(BedBlock.FACING))) {
            return;
        }

        List<BedEntity> anchored = level.getEntitiesOfClass(BedEntity.class, new AABB(head));
        if (!anchored.isEmpty()) {
            BedEntity entity = anchored.get(0);
            if (!entity.isDoubleBed() || entity.hasPassenger(clicker)) {
                return;
            }
            BlockPos partner = entity.getDoubleBedPos();
            if (!isMatchingHead(level, headState, partner)) {
                return;
            }
            shiftEntity(level, entity, head, partner);
            return;
        }

        Player sleeper = findDoubleSleeper(level, head);
        if (sleeper == null || sleeper == clicker) {
            return;
        }
        shiftSleeper(level, sleeper, head);
    }

    private static void shiftEntity(Level level, BedEntity entity, BlockPos from, BlockPos to) {
        entity.clearDoubleBed();
        setOccupied(level, from, false);
        BlockState destination = level.getBlockState(to);
        VoxelShape shape = destination.getShape(level, to);
        double height = shape.max(Direction.Axis.Y);
        if (!Double.isFinite(height)) {
            height = 0.5625;
        }
        entity.setPos(to.getX() + 0.5, to.getY() + height, to.getZ() + 0.5);
        setOccupied(level, to, true);
        for (Entity passenger : List.copyOf(entity.getPassengers())) {
            entity.positionRider(passenger);
        }
    }

    private static void shiftSleeper(Level level, Player sleeper, BlockPos clickedHead) {
        BlockPos sleepPos = sleeper.getSleepingPos().orElse(null);
        if (sleepPos == null) {
            return;
        }
        BlockState sleepState = level.getBlockState(sleepPos);
        if (!isBedHead(sleepState)) {
            return;
        }
        BlockPos partner = BedEntity.getDoubleBedPos(sleepPos, sleepState);
        if (!clickedHead.equals(sleepPos) && !clickedHead.equals(partner)) {
            return;
        }
        BlockPos stay = clickedHead.equals(sleepPos) ? partner : sleepPos;
        if (stay.equals(partner) && !isMatchingHead(level, sleepState, partner)) {
            return;
        }

        PlayerSleepData data = STPlatStuff.getPlayerSleepData(sleeper);
        data.setDoubleBed(false);
        if (!level.isClientSide && sleeper instanceof ServerPlayer serverPlayer && level instanceof ServerLevel serverLevel) {
            data.syncToClient(serverPlayer);
            if (!stay.equals(sleepPos)) {
                BlockPos respawn = serverPlayer.getRespawnPosition();
                if (respawn != null
                        && serverLevel.dimension().equals(serverPlayer.getRespawnDimension())
                        && (respawn.equals(sleepPos) || respawn.equals(partner))) {
                    serverPlayer.setRespawnPosition(
                            serverLevel.dimension(),
                            stay,
                            serverPlayer.getYRot(),
                            false,
                            false
                    );
                }
            }
        }

        setOccupied(level, stay, true);
        if (!level.isClientSide) {
            // Sleeping position is already baked in, including the double-bed offset.
            // Starting sleep again on the kept bed places them on that bed alone.
            sleeper.startSleeping(stay);
        }
        setOccupied(level, clickedHead, false);
    }

    private static Player findDoubleSleeper(Level level, BlockPos clickedHead) {
        for (Player player : level.players()) {
            if (!player.isSleeping()) {
                continue;
            }
            BlockPos sleepPos = player.getSleepingPos().orElse(null);
            if (sleepPos == null || !STPlatStuff.getPlayerSleepData(player).usingDoubleBed()) {
                continue;
            }
            BlockState sleepState = level.getBlockState(sleepPos);
            if (!isBedHead(sleepState)) {
                continue;
            }
            BlockPos partner = BedEntity.getDoubleBedPos(sleepPos, sleepState);
            if (clickedHead.equals(sleepPos) || clickedHead.equals(partner)) {
                return player;
            }
        }
        return null;
    }

    private static void setOccupied(Level level, BlockPos head, boolean occupied) {
        BlockState state = level.getBlockState(head);
        if (!isBed(state)) {
            return;
        }
        level.setBlockAndUpdate(head, state.setValue(BedBlock.OCCUPIED, occupied));
        if (state.getValue(BedBlock.PART) != BedPart.HEAD) {
            return;
        }
        BlockPos foot = head.relative(state.getValue(BedBlock.FACING).getOpposite());
        BlockState footState = level.getBlockState(foot);
        if (footState.is(state.getBlock())
                && footState.hasProperty(BedBlock.OCCUPIED)
                && footState.getValue(BedBlock.PART) == BedPart.FOOT) {
            level.setBlockAndUpdate(foot, footState.setValue(BedBlock.OCCUPIED, occupied));
        }
    }

    private static boolean bedBlocked(Level level, BlockPos head, Direction facing) {
        BlockPos above = head.above();
        if (level.getBlockState(above).isSuffocating(level, above)) {
            return true;
        }
        BlockPos footAbove = above.relative(facing.getOpposite());
        return level.getBlockState(footAbove).isSuffocating(level, footAbove);
    }

    private static boolean isMatchingHead(Level level, BlockState headState, BlockPos partner) {
        BlockState partnerState = level.getBlockState(partner);
        return partnerState.is(headState.getBlock())
                && isBedHead(partnerState)
                && partnerState.getValue(BedBlock.FACING) == headState.getValue(BedBlock.FACING);
    }

    private static BlockPos bedHead(BlockState state, BlockPos pos) {
        if (state.getValue(BedBlock.PART) == BedPart.FOOT) {
            return pos.relative(state.getValue(BedBlock.FACING));
        }
        return pos;
    }

    private static boolean isBedHead(BlockState state) {
        return isBed(state) && state.getValue(BedBlock.PART) == BedPart.HEAD;
    }

    private static boolean isBed(BlockState state) {
        return state.is(BlockTags.BEDS)
                && state.hasProperty(BedBlock.OCCUPIED)
                && state.hasProperty(BedBlock.PART)
                && state.hasProperty(BedBlock.FACING);
    }
}

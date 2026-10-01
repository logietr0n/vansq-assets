package com.vansqmod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public class LichenMossCarpetBlock extends CarpetBlock {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public LichenMossCarpetBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false));
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (LichenMossLighting.canLightFrom(entity)) {
            if (!state.getValue(LIT) && !level.isClientSide) {
                level.setBlock(pos, state.setValue(LIT, true), 2);
                level.scheduleTick(pos, this, 100);
            }
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (LichenMossLighting.canLightFrom(entity)) {
            if (!state.getValue(LIT) && !level.isClientSide) {
                level.setBlock(pos, state.setValue(LIT, true), 2);
                level.scheduleTick(pos, this, 100);
            }
        }
        super.entityInside(state, level, pos, entity);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean bl) {
        if (level.isClientSide) {
            return;
        }
        boolean lit = state.getValue(LIT);
        if (lit != level.hasNeighborSignal(pos)) {
            if (lit) {
                level.scheduleTick(pos, this, 4);
            } else {
                level.setBlock(pos, state.cycle(LIT), 2);
            }
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Carpet is only 1/16th tall; only treat entities as "standing on it"
        // if they intersect a thin slice above the block.
        AABB standingSlice = new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0D, pos.getY() + 0.2D, pos.getZ() + 1.0D);
        boolean noEntityStandingOnCarpet = level.getEntitiesOfClass(
                Entity.class, standingSlice, LichenMossLighting::keepsLit).isEmpty();
        boolean shouldUnlight = state.getValue(LIT) && !level.hasNeighborSignal(pos) && noEntityStandingOnCarpet;
        if (shouldUnlight) {
            level.setBlock(pos, state.cycle(LIT), 2);
        } else {
            level.scheduleTick(pos, this, 40);
        }
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return state.getValue(LIT);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIT);
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState base = super.getStateForPlacement(context);
        if (base == null) return null;
        return base.setValue(LIT, context.getLevel().hasNeighborSignal(context.getClickedPos()));
    }
}


package com.vansqmod.compat;

import com.vansqmod.VansqMod;
import com.vansqmod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.orcinus.galosphere.entities.Sparkle;
import net.orcinus.galosphere.init.GMemoryModuleTypes;

import java.util.Map;
import java.util.Optional;

/**
 * Extends Galosphere sparkle pollination without new sparkle variants: rhodoheart is added to
 * {@code #galosphere:crystal_clusters}, and conversion uses a fallback when the sparkle's variant
 * does not match the cluster block.
 */
@EventBusSubscriber(modid = VansqMod.MODID)
public final class GalosphereSparkleCompat {

    private static final String POLLINATED_CLUSTER_CLASS = "net.orcinus.galosphere.blocks.PollinatedClusterBlock";
    private static final Map<ResourceLocation, ResourceLocation> GLINTED_CLUSTER_PAIRS = Map.of(
            ResourceLocation.fromNamespaceAndPath("minecraft", "amethyst_cluster"),
            ResourceLocation.fromNamespaceAndPath("galosphere", "glinted_amethyst_cluster"),
            ResourceLocation.fromNamespaceAndPath("galosphere", "allurite_cluster"),
            ResourceLocation.fromNamespaceAndPath("galosphere", "glinted_allurite_cluster"),
            ResourceLocation.fromNamespaceAndPath("galosphere", "lumiere_cluster"),
            ResourceLocation.fromNamespaceAndPath("galosphere", "glinted_lumiere_cluster"),
            ResourceLocation.fromNamespaceAndPath("vansqmod", "rhodoheart_cluster"),
            ResourceLocation.fromNamespaceAndPath("vansqmod", "glinted_rhodoheart_cluster")
    );

    private static final ThreadLocal<BlockPos> POLLINATION_TARGET = new ThreadLocal<>();

    private static BooleanProperty galospherePollinatedProperty;
    private static Boolean slowedBuddingMining;

    private GalosphereSparkleCompat() {
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (!event.getState().is(ModBlocks.BUDDING_RHODOHEART.get())) {
            return;
        }
        if (ModList.get().isLoaded("galosphere") && isGalosphereSlowedBuddingMiningEnabled()) {
            event.setNewSpeed(2.0F);
            return;
        }
        if (ModList.get().isLoaded("caverns_and_chasms")) {
            event.setNewSpeed(event.getOriginalSpeed() / 8.0F);
        }
    }

    public static void capturePollinationTarget(Sparkle entity) {
        entity.getBrain().getMemory(GMemoryModuleTypes.NEAREST_POLLINATED_CLUSTER.get())
                .ifPresent(POLLINATION_TARGET::set);
    }

    public static void finishPollination(ServerLevel world, Sparkle entity, boolean cooldownOnly) {
        try {
            if (cooldownOnly) {
                return;
            }
            BlockPos pos = POLLINATION_TARGET.get();
            if (pos == null) {
                return;
            }
            BlockState clusterState = world.getBlockState(pos);
            if (!isPollinatableCluster(clusterState)) {
                return;
            }
            BlockState glintedState = buildGlintedClusterState(clusterState);
            if (glintedState == clusterState) {
                return;
            }
            world.setBlock(pos, glintedState, 2);
            world.playSound(
                    null,
                    pos,
                    glintedState.getSoundType().getBreakSound(),
                    SoundSource.BLOCKS,
                    1.0F,
                    1.0F
            );
            world.levelEvent(2005, pos, 0);
        } finally {
            POLLINATION_TARGET.remove();
        }
    }

    public static BlockState buildGlintedClusterState(BlockState clusterState) {
        Block glinted = resolveGlintedBlock(clusterState.getBlock());
        if (glinted == null) {
            return clusterState;
        }
        return glinted.withPropertiesOf(clusterState);
    }

    public static Block resolveGlintedBlock(Block source) {
        ResourceLocation sourceId = BuiltInRegistries.BLOCK.getKey(source);
        if (sourceId == null) {
            return null;
        }
        ResourceLocation glintedId = GLINTED_CLUSTER_PAIRS.get(sourceId);
        if (glintedId == null) {
            return null;
        }
        return BuiltInRegistries.BLOCK.get(glintedId);
    }

    public static boolean isPollinatableCluster(BlockState state) {
        if (state.is(Blocks.AMETHYST_CLUSTER)) {
            return true;
        }
        if (isUnpollinatedRhodoheartCluster(state)) {
            return true;
        }
        return isUnpollinatedGalosphereCluster(state);
    }

    public static boolean isUnpollinatedRhodoheartCluster(BlockState state) {
        return state.getBlock() instanceof com.vansqmod.block.RhodoheartPollinatedClusterBlock
                && !state.getValue(com.vansqmod.block.RhodoheartPollinatedClusterBlock.POLLINATED);
    }

    private static boolean isUnpollinatedGalosphereCluster(BlockState state) {
        BooleanProperty pollinated = galospherePollinatedProperty();
        if (pollinated == null) {
            return false;
        }
        try {
            Class<?> pollinatedClusterClass = Class.forName(POLLINATED_CLUSTER_CLASS);
            if (!pollinatedClusterClass.isInstance(state.getBlock())) {
                return false;
            }
            return !state.getValue(pollinated);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static BooleanProperty galospherePollinatedProperty() {
        if (galospherePollinatedProperty != null) {
            return galospherePollinatedProperty;
        }
        try {
            Class<?> pollinatedClusterClass = Class.forName(POLLINATED_CLUSTER_CLASS);
            galospherePollinatedProperty = (BooleanProperty) pollinatedClusterClass.getField("POLLINATED").get(null);
        } catch (ReflectiveOperationException e) {
            galospherePollinatedProperty = null;
        }
        return galospherePollinatedProperty;
    }

    private static boolean isGalosphereSlowedBuddingMiningEnabled() {
        if (slowedBuddingMining != null) {
            return slowedBuddingMining;
        }
        try {
            Class<?> config = Class.forName("net.orcinus.galosphere.config.GalosphereConfig");
            Object configValue = config.getField("SLOWED_BUDDING_AMETHYST_MINING_SPEED").get(null);
            slowedBuddingMining = (Boolean) configValue.getClass().getMethod("get").invoke(configValue);
        } catch (ReflectiveOperationException e) {
            slowedBuddingMining = true;
        }
        return slowedBuddingMining;
    }
}

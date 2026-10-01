package com.vansqmod.integration.yungscavebiomes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

/**
 * Updates ice_sheet {@code lit} from attached {@code creeping_ice_glows_on} neighbors
 * (ores). Used after chunk load — never during worldgen, and never via blocking
 * {@code getChunk}.
 */
public final class IceSheetGlowHelper {

    private static final ResourceLocation ICE_SHEET_ID =
            ResourceLocation.fromNamespaceAndPath("yungscavebiomes", "ice_sheet");

    private static final TagKey<Block> CREEPING_ICE_GLOWS_ON = TagKey.create(
            Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath("yungscavebiomes", "creeping_ice_glows_on")
    );

    private IceSheetGlowHelper() {
    }

    public static boolean mayHaveIceSheet(LevelChunk chunk) {
        Block iceSheet = iceSheetBlock();
        if (iceSheet.defaultBlockState().isAir()) {
            return false;
        }
        for (LevelChunkSection section : chunk.getSections()) {
            if (section != null && !section.hasOnlyAir() && section.maybeHas(state -> state.is(iceSheet))) {
                return true;
            }
        }
        return false;
    }

    private static final int[] NEIGHBOR_DX = {-1, 1, 0, 0};
    private static final int[] NEIGHBOR_DZ = {0, 0, -1, 1};

    public static boolean mayHaveGlowOre(LevelChunk chunk) {
        for (LevelChunkSection section : chunk.getSections()) {
            if (section != null && !section.hasOnlyAir() && section.maybeHas(state -> state.is(CREEPING_ICE_GLOWS_ON))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Cheap palette probe: skip the ice-sheet walk when this chunk and its loaded
     * neighbors have no {@code creeping_ice_glows_on} ores.
     */
    public static boolean mayHaveNearbyGlowOre(ServerLevel level, LevelChunk chunk) {
        if (mayHaveGlowOre(chunk)) {
            return true;
        }
        ChunkPos pos = chunk.getPos();
        for (int i = 0; i < NEIGHBOR_DX.length; i++) {
            LevelChunk neighbor = level.getChunkSource().getChunkNow(
                    pos.x + NEIGHBOR_DX[i],
                    pos.z + NEIGHBOR_DZ[i]
            );
            if (neighbor != null && mayHaveGlowOre(neighbor)) {
                return true;
            }
        }
        return false;
    }

    public static void fixChunk(ServerLevel level, LevelChunk chunk) {
        fixChunk(level, chunk, Integer.MAX_VALUE);
    }

    /**
     * @return {@code false} if {@code maxUpdates} was hit before every ice sheet was resolved
     */
    public static boolean fixChunk(ServerLevel level, LevelChunk chunk, int maxUpdates) {
        Block iceSheet = iceSheetBlock();
        if (iceSheet.defaultBlockState().isAir() || maxUpdates <= 0) {
            return maxUpdates > 0;
        }

        int updates = 0;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        LevelChunkSection[] sections = chunk.getSections();

        for (int sectionIndex = 0; sectionIndex < sections.length; sectionIndex++) {
            LevelChunkSection section = sections[sectionIndex];
            if (section == null || section.hasOnlyAir() || !section.maybeHas(state -> state.is(iceSheet))) {
                continue;
            }

            int minY = chunk.getSectionYFromSectionIndex(sectionIndex) << 4;
            int minX = chunk.getPos().getMinBlockX();
            int minZ = chunk.getPos().getMinBlockZ();

            for (int y = 0; y < 16; y++) {
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        BlockState state = section.getBlockState(x, y, z);
                        if (!state.is(iceSheet) || !state.hasProperty(BlockStateProperties.LIT)) {
                            continue;
                        }

                        cursor.set(minX + x, minY + y, minZ + z);
                        GlowProbe probe = probeGlow(state, level, chunk, cursor);
                        if (probe == GlowProbe.UNKNOWN) {
                            continue;
                        }
                        boolean shouldGlow = probe == GlowProbe.LIT;
                        if (state.getValue(BlockStateProperties.LIT) == shouldGlow) {
                            continue;
                        }

                        // Lit does not change the block shape. Neighbor shape updates load
                        // surrounding chunks on the server thread and were stalling ticks.
                        level.setBlock(
                                cursor,
                                state.setValue(BlockStateProperties.LIT, shouldGlow),
                                Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE
                        );
                        if (++updates >= maxUpdates) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private static Block cachedIceSheet;

    private static Block iceSheetBlock() {
        if (cachedIceSheet == null || cachedIceSheet.defaultBlockState().isAir()) {
            cachedIceSheet = BuiltInRegistries.BLOCK.get(ICE_SHEET_ID);
        }
        return cachedIceSheet;
    }

    /**
     * {@link GlowProbe#UNKNOWN} means an attached face is in an unloaded chunk.
     * Leave {@code lit} alone so sheets on ores across a chunk border light when
     * that neighbor loads, instead of being forced off or blocking the server thread.
     */
    private static GlowProbe probeGlow(BlockState state, ServerLevel level, LevelChunk origin, BlockPos pos) {
        boolean sawUnloaded = false;
        for (Direction direction : Direction.values()) {
            if (!MultifaceBlock.hasFace(state, direction)) {
                continue;
            }
            BlockState neighbor = loadedBlockState(level, origin, pos.relative(direction));
            if (neighbor == null) {
                sawUnloaded = true;
                continue;
            }
            if (neighbor.is(CREEPING_ICE_GLOWS_ON)) {
                return GlowProbe.LIT;
            }
        }
        return sawUnloaded ? GlowProbe.UNKNOWN : GlowProbe.UNLIT;
    }

    private static BlockState loadedBlockState(ServerLevel level, LevelChunk origin, BlockPos pos) {
        int chunkX = SectionPos.blockToSectionCoord(pos.getX());
        int chunkZ = SectionPos.blockToSectionCoord(pos.getZ());
        ChunkPos originPos = origin.getPos();
        if (chunkX == originPos.x && chunkZ == originPos.z) {
            return origin.getBlockState(pos);
        }
        LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
        return chunk == null ? null : chunk.getBlockState(pos);
    }

    private enum GlowProbe {
        LIT,
        UNLIT,
        UNKNOWN
    }
}

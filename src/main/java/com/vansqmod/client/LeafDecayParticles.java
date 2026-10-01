package com.vansqmod.client;

import com.vansqmod.compat.CustomFallingLeaves;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Spawns 8–16 falling-leaf particles of the decaying block's type throughout
 * the block volume, and plays leaf-litter break at the block. Subtle Effects'
 * destroy crumbs/sound are disabled in config.
 */
@OnlyIn(Dist.CLIENT)
public final class LeafDecayParticles {

    private static final int MIN_COUNT = 8;
    private static final int MAX_COUNT = 16;
    private static final ResourceLocation LEAF_LITTER_BREAK =
            ResourceLocation.withDefaultNamespace("block.leaf_litter.break");
    private static final TagKey<Block> FALLING_NEEDLES = TagKey.create(
            Registries.BLOCK,
            ResourceLocation.withDefaultNamespace("spawn_falling_needles")
    );

    private LeafDecayParticles() {
    }

    public static void trySpawnFromSubtleEffects(ClientLevel level, Object payload) {
        if (level == null || payload == null) {
            return;
        }
        try {
            Object config = payload.getClass().getMethod("config").invoke(payload);
            if (!(config instanceof Enum<?> type) || !"LEAVES_DECAY".equals(type.name())) {
                return;
            }
            BlockPos pos = (BlockPos) payload.getClass().getMethod("pos").invoke(payload);
            int stateId = (Integer) payload.getClass().getMethod("stateId").invoke(payload);
            spawn(level, pos, Block.stateById(stateId));
        } catch (ReflectiveOperationException ignored) {
        }
    }

    public static void spawn(ClientLevel level, BlockPos pos, BlockState state) {
        playDecaySound(level, pos);
        ParticleOptions options = particleFor(level, pos, state);
        if (options == null) {
            return;
        }
        RandomSource random = level.getRandom();
        int count = MIN_COUNT + random.nextInt(MAX_COUNT - MIN_COUNT + 1);
        int gridY = Math.max(2, (count + 3) / 4);
        for (int i = 0; i < count; i++) {
            int gx = i % 2;
            int gz = (i / 2) % 2;
            int gy = i / 4;
            double x = pos.getX() + (gx + 0.5 + (random.nextDouble() - 0.5) * 0.7) / 2.0;
            double y = pos.getY() + (gy + 0.5 + (random.nextDouble() - 0.5) * 0.7) / gridY;
            double z = pos.getZ() + (gz + 0.5 + (random.nextDouble() - 0.5) * 0.7) / 2.0;
            level.addParticle(options, x, y, z, 0.0D, 0.0D, 0.0D);
        }
    }

    private static void playDecaySound(ClientLevel level, BlockPos pos) {
        BuiltInRegistries.SOUND_EVENT.getOptional(LEAF_LITTER_BREAK).ifPresent(sound ->
                level.playLocalSound(
                        pos.getX() + 0.5D,
                        pos.getY() + 0.5D,
                        pos.getZ() + 0.5D,
                        sound,
                        SoundSource.BLOCKS,
                        0.5F,
                        0.75F,
                        false
                )
        );
    }

    private static ParticleOptions particleFor(ClientLevel level, BlockPos pos, BlockState state) {
        SimpleParticleType custom = CustomFallingLeaves.getParticle(state);
        if (custom != null) {
            return custom;
        }
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (state.is(Blocks.CHERRY_LEAVES)) {
            return ParticleTypes.CHERRY_LEAVES;
        }
        if ("pale_oak_leaves".equals(id.getPath())) {
            ParticleType<?> paleOak = BuiltInRegistries.PARTICLE_TYPE.getOptional(
                    ResourceLocation.withDefaultNamespace("pale_oak_leaves")).orElse(null);
            if (paleOak instanceof SimpleParticleType simple) {
                return simple;
            }
        }
        String tintedName = state.is(FALLING_NEEDLES) ? "tinted_needles" : "tinted_leaves";
        ParticleType<?> tinted = BuiltInRegistries.PARTICLE_TYPE.getOptional(
                ResourceLocation.withDefaultNamespace(tintedName)).orElse(null);
        if (tinted != null) {
            return ColorParticleOption.create(unchecked(tinted), leafColor(level, pos, state));
        }
        return ParticleTypes.CHERRY_LEAVES;
    }

    @SuppressWarnings("unchecked")
    private static ParticleType<ColorParticleOption> unchecked(ParticleType<?> type) {
        return (ParticleType<ColorParticleOption>) type;
    }

    private static int leafColor(ClientLevel level, BlockPos pos, BlockState state) {
        try {
            Class<?> listener = Class.forName(
                    "com.blackgear.vanillabackport.client.resources.color.LeafColorReloadListener");
            Block block = state.getBlock();
            Boolean custom = (Boolean) listener.getMethod("hasCustomColor", Block.class).invoke(null, block);
            if (Boolean.TRUE.equals(custom)) {
                return (Integer) listener.getMethod("getCustomColor", Block.class).invoke(null, block);
            }
        } catch (Throwable ignored) {
        }
        int color = Minecraft.getInstance().getBlockColors().getColor(state, level, pos, 0);
        return color == -1 ? 0xFFFFFF : color;
    }
}

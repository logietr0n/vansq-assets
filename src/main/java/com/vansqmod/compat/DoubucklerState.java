package com.vansqmod.compat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record DoubucklerState(long mainReadyAt, long offReadyAt, int animHand) {

    public static final int ANIM_NONE = 0;
    public static final int ANIM_MAIN = 1;
    public static final int ANIM_OFF = 2;

    public static final DoubucklerState EMPTY = new DoubucklerState(0L, 0L, ANIM_NONE);

    public static final Codec<DoubucklerState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.optionalFieldOf("main_ready", 0L).forGetter(DoubucklerState::mainReadyAt),
            Codec.LONG.optionalFieldOf("off_ready", 0L).forGetter(DoubucklerState::offReadyAt),
            Codec.INT.optionalFieldOf("anim", ANIM_NONE).forGetter(DoubucklerState::animHand)
    ).apply(instance, DoubucklerState::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DoubucklerState> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG,
            DoubucklerState::mainReadyAt,
            ByteBufCodecs.VAR_LONG,
            DoubucklerState::offReadyAt,
            ByteBufCodecs.VAR_INT,
            DoubucklerState::animHand,
            DoubucklerState::new
    );

    public int remaining(boolean main, long gameTime) {
        long readyAt = main ? mainReadyAt : offReadyAt;
        return (int) Math.max(0L, readyAt - gameTime);
    }

    public boolean isIdle(long gameTime) {
        return remaining(true, gameTime) <= 0 && remaining(false, gameTime) <= 0;
    }

    public DoubucklerState withReadyAt(boolean main, long readyAt) {
        return main
                ? new DoubucklerState(readyAt, offReadyAt, animHand)
                : new DoubucklerState(mainReadyAt, readyAt, animHand);
    }

    public DoubucklerState withAnim(int hand) {
        return new DoubucklerState(mainReadyAt, offReadyAt, hand);
    }
}

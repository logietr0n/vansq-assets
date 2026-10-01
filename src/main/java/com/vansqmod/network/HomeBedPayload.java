package com.vansqmod.network;

import com.vansqmod.VansqMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Server → client: Sleep Tight home bed position, used for Reactive Music's HOME radius.
 */
public record HomeBedPayload(boolean present, String dimension, int x, int y, int z) implements CustomPacketPayload {

    public static final Type<HomeBedPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "home_bed"));

    public static final StreamCodec<RegistryFriendlyByteBuf, HomeBedPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, HomeBedPayload::present,
                    ByteBufCodecs.STRING_UTF8, HomeBedPayload::dimension,
                    ByteBufCodecs.VAR_INT, HomeBedPayload::x,
                    ByteBufCodecs.VAR_INT, HomeBedPayload::y,
                    ByteBufCodecs.VAR_INT, HomeBedPayload::z,
                    HomeBedPayload::new
            );

    public static HomeBedPayload clear() {
        return new HomeBedPayload(false, "", 0, 0, 0);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

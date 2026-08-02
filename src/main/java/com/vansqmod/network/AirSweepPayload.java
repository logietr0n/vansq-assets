package com.vansqmod.network;

import com.vansqmod.VansqMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Client → server: full-strength air click with a sweeping weapon (Combat Nouveau absent).
 */
public record AirSweepPayload() implements CustomPacketPayload {

    public static final Type<AirSweepPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "air_sweep"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AirSweepPayload> STREAM_CODEC =
            StreamCodec.unit(new AirSweepPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

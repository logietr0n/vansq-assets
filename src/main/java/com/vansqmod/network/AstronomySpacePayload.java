package com.vansqmod.network;

import com.vansqmod.VansqMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Full Spyglass Astronomy snapshot. Server → client on join and after another
 * player changes the shared sky. Client → server when this player names or
 * draws something, or when a LAN host bootstraps empty world data.
 */
public record AstronomySpacePayload(String snapshot) implements CustomPacketPayload {

    public static final int MAX_LENGTH = 1_000_000;

    public static final Type<AstronomySpacePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "astronomy_space"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AstronomySpacePayload> STREAM_CODEC =
            StreamCodec.of(AstronomySpacePayload::encode, AstronomySpacePayload::decode);

    private static void encode(RegistryFriendlyByteBuf buf, AstronomySpacePayload payload) {
        buf.writeUtf(payload.snapshot == null ? "" : payload.snapshot, MAX_LENGTH);
    }

    private static AstronomySpacePayload decode(RegistryFriendlyByteBuf buf) {
        return new AstronomySpacePayload(buf.readUtf(MAX_LENGTH));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

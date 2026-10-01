package com.vansqmod.network;

import com.vansqmod.VansqMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Client → server: this player's current Spyglass Astronomy snapshot should
 * become the shared world sky (names, constellations, seeds).
 */
public record AstronomySpaceUpdatePayload(String snapshot) implements CustomPacketPayload {

    public static final Type<AstronomySpaceUpdatePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "astronomy_space_update"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AstronomySpaceUpdatePayload> STREAM_CODEC =
            StreamCodec.of(AstronomySpaceUpdatePayload::encode, AstronomySpaceUpdatePayload::decode);

    private static void encode(RegistryFriendlyByteBuf buf, AstronomySpaceUpdatePayload payload) {
        buf.writeUtf(payload.snapshot == null ? "" : payload.snapshot, AstronomySpacePayload.MAX_LENGTH);
    }

    private static AstronomySpaceUpdatePayload decode(RegistryFriendlyByteBuf buf) {
        return new AstronomySpaceUpdatePayload(buf.readUtf(AstronomySpacePayload.MAX_LENGTH));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

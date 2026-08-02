package com.vansqmod.network;

import com.vansqmod.VansqMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Server → client: temporarily re-enable Distant Horizons' vanilla fog override
 * for Lost Caves sandstorms (imminent or active).
 */
public record SandstormVanillaFogPayload(boolean enable) implements CustomPacketPayload {

    public static final Type<SandstormVanillaFogPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "sandstorm_vanilla_fog"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SandstormVanillaFogPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL,
                    SandstormVanillaFogPayload::enable,
                    SandstormVanillaFogPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

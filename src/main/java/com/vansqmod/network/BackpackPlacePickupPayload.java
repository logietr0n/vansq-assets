package com.vansqmod.network;

import com.vansqmod.VansqMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Client → server request to place the equipped Curios backpack or pick up a placed one.
 */
public record BackpackPlacePickupPayload() implements CustomPacketPayload {

    public static final Type<BackpackPlacePickupPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "backpack_place_pickup"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BackpackPlacePickupPayload> STREAM_CODEC =
            StreamCodec.unit(new BackpackPlacePickupPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

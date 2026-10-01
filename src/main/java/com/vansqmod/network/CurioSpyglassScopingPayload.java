package com.vansqmod.network;

import com.vansqmod.VansqMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Client → server: the Spyglass Improvements hotkey is scoping a curio spyglass.
 */
public record CurioSpyglassScopingPayload(boolean scoping) implements CustomPacketPayload {

    public static final Type<CurioSpyglassScopingPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "curio_spyglass_scoping"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CurioSpyglassScopingPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL,
                    CurioSpyglassScopingPayload::scoping,
                    CurioSpyglassScopingPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

package com.vansqmod.network;

import com.vansqmod.VansqMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/**
 * Server → client: tint a vanilla boss bar with an RGB color, or drop the tint.
 */
public record BossBarColorPayload(UUID barId, int rgb, boolean present) implements CustomPacketPayload {

    public static final Type<BossBarColorPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "boss_bar_color"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BossBarColorPayload> STREAM_CODEC =
            StreamCodec.of(BossBarColorPayload::encode, BossBarColorPayload::decode);

    public static BossBarColorPayload set(UUID barId, int rgb) {
        return new BossBarColorPayload(barId, rgb, true);
    }

    public static BossBarColorPayload clear(UUID barId) {
        return new BossBarColorPayload(barId, 0, false);
    }

    private static void encode(RegistryFriendlyByteBuf buf, BossBarColorPayload payload) {
        buf.writeUUID(payload.barId);
        buf.writeInt(payload.rgb);
        buf.writeBoolean(payload.present);
    }

    private static BossBarColorPayload decode(RegistryFriendlyByteBuf buf) {
        return new BossBarColorPayload(buf.readUUID(), buf.readInt(), buf.readBoolean());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

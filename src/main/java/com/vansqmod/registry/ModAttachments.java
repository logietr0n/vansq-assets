package com.vansqmod.registry;

import com.mojang.serialization.Codec;
import com.vansqmod.VansqMod;
import com.vansqmod.compat.DoubucklerState;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public final class ModAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, VansqMod.MODID);

    /**
     * Latched once a Missionary or Mutant Enderman targets a player. Synced so
     * client boss music can follow the same fight-active gate as the bar.
     */
    public static final Supplier<AttachmentType<Boolean>> BOSS_FIGHT_ACTIVE =
            ATTACHMENT_TYPES.register("boss_fight_active", () -> AttachmentType.builder(() -> Boolean.FALSE)
                    .serialize(Codec.BOOL)
                    .sync(ByteBufCodecs.BOOL)
                    .build());

    /**
     * Spyglass Improvements' curio hotkey zooms without starting item use, so the
     * spyglass arm pose never starts. Synced while that hotkey is held.
     */
    public static final Supplier<AttachmentType<Boolean>> CURIO_SPYGLASS_SCOPING =
            ATTACHMENT_TYPES.register("curio_spyglass_scoping", () -> AttachmentType.builder(() -> Boolean.FALSE)
                    .sync(ByteBufCodecs.BOOL)
                    .build());

    public static final Supplier<AttachmentType<DoubucklerState>> DOUBUCKLER_HANDS =
            ATTACHMENT_TYPES.register("doubuckler_hands", () -> AttachmentType.builder(() -> DoubucklerState.EMPTY)
                    .serialize(DoubucklerState.CODEC)
                    .sync(DoubucklerState.STREAM_CODEC)
                    .build());

    private ModAttachments() {
    }
}

package com.vansqmod.mixin.subtleeffects;

import com.vansqmod.client.LeafDecayParticles;
import einstein.subtle_effects.networking.clientbound.ClientBoundBlockDestroyEffectsPayload;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Subtle Effects still notifies clients when leaves decay. Destroy crumbs/sound
 * are turned off in {@code config/subtle_effects/blocks.toml} ({@code leavesDecayEffects}).
 * This only spawns falling-leaf particles for that packet.
 */
@Mixin(targets = "einstein.subtle_effects.networking.clientbound.ClientPacketHandlers", remap = false)
public class SubtleEffectsLeafDecayMixin {

    @Inject(
            method = "handle(Lnet/minecraft/client/multiplayer/ClientLevel;Leinstein/subtle_effects/networking/clientbound/ClientBoundBlockDestroyEffectsPayload;)V",
            at = @At("HEAD")
    )
    private static void vansqmod$spawnLeafDecayParticles(
            ClientLevel level,
            ClientBoundBlockDestroyEffectsPayload payload,
            CallbackInfo ci
    ) {
        LeafDecayParticles.trySpawnFromSubtleEffects(level, payload);
    }
}

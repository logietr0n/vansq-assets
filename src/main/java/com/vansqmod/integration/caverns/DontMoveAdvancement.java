package com.vansqmod.integration.caverns;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * Don't. Move. is granted only when a Peeper is targeting the player within 32 blocks.
 */
public final class DontMoveAdvancement {

    public static final float RANGE = 32.0F;

    private static final ResourceLocation ADVANCEMENT =
            ResourceLocation.fromNamespaceAndPath("caverns_and_chasms", "adventure/dont_move");
    private static final String CRITERION = "spotted_by_peeper";

    private DontMoveAdvancement() {
    }

    public static void tryGrant(Entity peeper, LivingEntity target) {
        if (peeper == null || !(target instanceof ServerPlayer player) || !player.isAlive()) {
            return;
        }
        if (peeper.distanceTo(player) > RANGE) {
            return;
        }
        AdvancementHolder holder = player.server.getAdvancements().get(ADVANCEMENT);
        if (holder != null) {
            player.getAdvancements().award(holder, CRITERION);
        }
    }
}

package com.vansqmod.client;

import com.vansqmod.integration.curios.CurioSpyglass;
import com.vansqmod.registry.ModAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.neoforged.fml.ModList;

/**
 * Spyglass Improvements' curio hotkey sets {@code force_spyglass} and never starts
 * item use, so vanilla and Not Enough Animations never see a spyglass arm pose.
 */
public final class CurioSpyglassPose {

    private CurioSpyglassPose() {
    }

    public static boolean isPosing(Player player) {
        if (player == null) {
            return false;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (player == minecraft.player) {
            return isLocalHotkey(player);
        }
        return player.getData(ModAttachments.CURIO_SPYGLASS_SCOPING.get());
    }

    public static boolean poseHand(Player player, InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND && isPosing(player);
    }

    public static boolean poseArm(Player player, HumanoidArm arm) {
        return player.getMainArm() == arm && isPosing(player);
    }

    public static boolean isLocalHotkey(Player player) {
        if (!ModList.get().isLoaded("spyglass_improvements") || !ModList.get().isLoaded("curios")) {
            return false;
        }
        if (player.isUsingItem() && player.getUseItem().is(Items.SPYGLASS)) {
            return false;
        }
        return SpyglassZooming.isImprovementsZoom() && CurioSpyglass.has(player);
    }
}

package com.vansqmod.integration.beltborne;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.oxcodsnet.beltborne_lanterns.common.BeltState;
import net.oxcodsnet.beltborne_lanterns.common.DynamicLightsCompat;
import net.oxcodsnet.beltborne_lanterns.common.persistence.BeltLanternSave;
import net.oxcodsnet.beltborne_lanterns.neoforge.BeltNetworking;

import java.util.Objects;

final class BeltborneLanternSync {

    private BeltborneLanternSync() {
    }

    static void applyEquipped(ServerPlayer player, ItemStack stack) {
        if (stack.isEmpty() || !BeltborneLanternEquipment.isLamp(stack)) {
            clear(player);
            return;
        }

        ItemStack equipped = stack.copyWithCount(1);
        BeltState.setLamp(player, equipped);
        BeltLanternSave.get(server(player)).set(player.getUUID(), equipped);
        BeltNetworking.broadcastBeltState(player, equipped.getItem());
        DynamicLightsCompat.addFor(player);
    }

    static void clear(ServerPlayer player) {
        BeltState.setLamp(player, (ItemStack) null);
        BeltLanternSave.get(server(player)).set(player.getUUID(), (ItemStack) null);
        BeltNetworking.broadcastBeltState(player, (Item) null);
        DynamicLightsCompat.removeFor(player);
    }

    private static net.minecraft.server.MinecraftServer server(ServerPlayer player) {
        return Objects.requireNonNull(((ServerLevel) player.level()).getServer(), "Missing server instance");
    }
}

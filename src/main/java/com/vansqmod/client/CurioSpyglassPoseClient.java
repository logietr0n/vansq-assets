package com.vansqmod.client;

import com.vansqmod.VansqMod;
import com.vansqmod.network.CurioSpyglassScopingPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = VansqMod.MODID, value = Dist.CLIENT)
public final class CurioSpyglassPoseClient {

    private static boolean lastSent;

    private CurioSpyglassPoseClient() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.getConnection() == null) {
            lastSent = false;
            return;
        }
        boolean scoping = CurioSpyglassPose.isLocalHotkey(minecraft.player);
        if (scoping == lastSent) {
            return;
        }
        lastSent = scoping;
        PacketDistributor.sendToServer(new CurioSpyglassScopingPayload(scoping));
    }
}

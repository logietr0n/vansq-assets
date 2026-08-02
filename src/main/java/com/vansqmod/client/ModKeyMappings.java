package com.vansqmod.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.vansqmod.VansqMod;
import com.vansqmod.network.BackpackPlacePickupPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = VansqMod.MODID, value = Dist.CLIENT)
public final class ModKeyMappings {

    public static final String CATEGORY = "key.categories.vansqmod";

    public static KeyMapping PLACE_PICKUP_BACKPACK;

    private ModKeyMappings() {
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        PLACE_PICKUP_BACKPACK = new KeyMapping(
                "key.vansqmod.place_pickup_backpack",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                CATEGORY
        );
        event.register(PLACE_PICKUP_BACKPACK);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null || mc.getConnection() == null) {
            return;
        }
        if (PLACE_PICKUP_BACKPACK != null && PLACE_PICKUP_BACKPACK.consumeClick()) {
            PacketDistributor.sendToServer(new BackpackPlacePickupPayload());
        }
    }
}

package com.vansqmod.client;

import com.mojang.brigadier.Command;
import com.vansqmod.VansqMod;
import com.vansqmod.config.ObliteratorItemConfig;
import com.vansqmod.config.VansqModClientConfigs;
import net.minecraft.commands.Commands;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

@EventBusSubscriber(modid = VansqMod.MODID, value = Dist.CLIENT)
public final class CustomTooltipCommands {

    private CustomTooltipCommands() {
    }

    @SubscribeEvent
    public static void registerClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("vansq")
                        .then(Commands.literal("reloadconfig")
                                .executes(ctx -> {
                                    VansqModClientConfigs.loadAll();
                                    ObliteratorItemConfig.load();
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(Commands.literal("reloadtooltips")
                                .executes(ctx -> {
                                    VansqModClientConfigs.loadAll();
                                    ObliteratorItemConfig.load();
                                    return Command.SINGLE_SUCCESS;
                                }))
        );
    }
}

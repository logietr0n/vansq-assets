package com.vansqmod.client;

import com.mojang.brigadier.Command;
import com.vansqmod.VansqMod;
import com.vansqmod.config.CustomTooltipConfig;
import com.vansqmod.config.ItemNameColorConfig;
import com.vansqmod.config.VansqModClientConfigs;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
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
                Commands.literal("vansqmod")
                        .then(Commands.literal("reloadconfig")
                                .executes(ctx -> {
                                    VansqModClientConfigs.loadAll();
                                    ctx.getSource().sendSuccess(
                                            () -> Component.literal(reloadSummary()),
                                            true
                                    );
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(Commands.literal("reloadtooltips")
                                .executes(ctx -> {
                                    VansqModClientConfigs.loadAll();
                                    ctx.getSource().sendSuccess(
                                            () -> Component.literal(reloadSummary()),
                                            true
                                    );
                                    return Command.SINGLE_SUCCESS;
                                }))
        );
    }

    private static String reloadSummary() {
        int tooltips = CustomTooltipConfig.entryCount();
        int nameColors = ItemNameColorConfig.entryCount();
        return "Reloaded vansqmod configs ("
                + tooltips + " tooltip"
                + (tooltips == 1 ? "" : "s")
                + ", "
                + nameColors + " name color"
                + (nameColors == 1 ? "" : "s")
                + ").";
    }
}

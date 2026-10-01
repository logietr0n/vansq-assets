package com.vansqmod.entity;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.vansqmod.config.BlockedEntityConfig;
import com.vansqmod.config.ObliteratorItemConfig;
import com.vansqmod.debug.VansqDebugState;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class VansqModCommands {

    private VansqModCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("vansq")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("reloadconfig")
                                .executes(ctx -> {
                                    BlockedEntityConfig.load();
                                    ObliteratorItemConfig.load();
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(Commands.literal("debug")
                                .then(Commands.literal("equip")
                                        .executes(ctx -> {
                                            VansqDebugState.setForceEquipmentEnabled(
                                                    !VansqDebugState.isForceEquipmentEnabled());
                                            return Command.SINGLE_SUCCESS;
                                        })
                                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                                .executes(ctx -> {
                                                    VansqDebugState.setForceEquipmentEnabled(
                                                            BoolArgumentType.getBool(ctx, "enabled"));
                                                    return Command.SINGLE_SUCCESS;
                                                })))
                                .then(Commands.literal("mobenchant")
                                        .executes(ctx -> {
                                            VansqDebugState.setForceMobEnchantEnabled(
                                                    !VansqDebugState.isForceMobEnchantEnabled());
                                            return Command.SINGLE_SUCCESS;
                                        })
                                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                                .executes(ctx -> {
                                                    VansqDebugState.setForceMobEnchantEnabled(
                                                            BoolArgumentType.getBool(ctx, "enabled"));
                                                    return Command.SINGLE_SUCCESS;
                                                })))
                                .then(Commands.literal("chaos")
                                        .executes(ctx -> toggleChaos(ctx.getSource()))
                                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                                .executes(ctx -> setChaos(
                                                        ctx.getSource(),
                                                        BoolArgumentType.getBool(ctx, "enabled")))))
                                .then(Commands.literal("playercount")
                                        .then(Commands.argument("count", IntegerArgumentType.integer(0, 64))
                                                .executes(ctx -> {
                                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                                    int count = IntegerArgumentType.getInteger(ctx, "count");
                                                    VansqDebugState.setFakePlayerCount(player.getUUID(), count);
                                                    return Command.SINGLE_SUCCESS;
                                                }))))
        );
    }

    private static int toggleChaos(CommandSourceStack source) {
        return setChaos(source, !VansqDebugState.isChaosEnabled());
    }

    private static int setChaos(CommandSourceStack source, boolean enabled) {
        VansqDebugState.setForceRareEventsEnabled(enabled);
        source.sendSuccess(
                () -> Component.literal("vansq debug chaos: " + (enabled ? "on" : "off")),
                true);
        return Command.SINGLE_SUCCESS;
    }
}

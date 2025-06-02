package com.exiledpomegranate;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Objects;

public class BarrelBombCommand {
    private static final List<String> parameterNames = List.of("power", "penetration", "dropPercentage", "directionalOffset");

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("barrelbombs")
                .executes(BarrelBombCommand::help)
                .then(CommandManager.argument("setting", StringArgumentType.word())
                        .suggests(suggestSettings())
                        .executes(BarrelBombCommand::getValue)
                        .then(CommandManager.argument("value", StringArgumentType.greedyString())
                                .suggests((context, builder) -> {
                                    builder.suggest("default");
                                    return builder.buildFuture();
                                })
                                .executes(BarrelBombCommand::setValue))
                )
                .then(CommandManager.literal("help")
                        .executes(BarrelBombCommand::help)
                )
        );
    }

    private static SuggestionProvider<ServerCommandSource> suggestSettings() {
        return (context, builder) -> {
            for (String name : parameterNames) {
                builder.suggest(name);
            }
            return builder.buildFuture();
        };
    }

    private static int help(CommandContext<ServerCommandSource> ctx) {
        ctx.getSource().sendFeedback(() -> Text.literal(
                """
                        Barrel Bombs config command
                        /barrelbombs - View this help message
                        /barrelbombs help - View this help message
                        /barrelbombs <setting> - View a setting's value
                        /barrelbombs <setting> <value> - Set a setting's value
                        /barrelbombs <setting> default - Set a setting's value to the default
                        On servers, settings can only be set by OPs
                        Some values can only be set in the config at /.minecraft/config/barrelbombs.conf"""
        ), false);
        return 1;
    }

    private static int getValue(CommandContext<ServerCommandSource> context) {
        String key = StringArgumentType.getString(context, "setting");
        if (!parameterNames.contains(key)) {
            context.getSource().sendError(Text.literal("Unknown setting: " + key));
            return 0;
        }
        String value = getConfigValue(key);
        context.getSource().sendFeedback(() -> Text.literal("Value of " + key + ": " + value), false);
        return 1;
    }

    private static int setValue(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String key = StringArgumentType.getString(context, "setting");
        String value = StringArgumentType.getString(context, "value");

        if (!hasConfigPermission(source)) {
            source.sendError(Text.literal("You do not have permission to set config values."));
            return 0;
        }

        if (!parameterNames.contains(key)) {
            source.sendError(Text.literal("Unknown setting: " + key));
            return 0;
        }

        ConfigHandler.dirty = true;

        boolean success = setConfigValue(key, value);

        if (success) {
            source.sendFeedback(() -> Text.literal("Set BarrelBombs " + key + " to " + value), true);
            return 1;
        } else {
            source.sendError(Text.literal("Failed to set " + key + ". Check that your value is formatted properly."));
            return 0;
        }
    }

    private static boolean hasConfigPermission(ServerCommandSource source) {
        MinecraftServer server = source.getServer();

        if (!server.isSingleplayer()) {
            // Dedicated server — must be OP
            return source.hasPermissionLevel(2);
        } else {
            // Singleplayer or LAN
            String senderName = source.getName();
            String hostName = server.getHostProfile().getName();

            // If the sender is the host, or if they have permission level 2 (cheats enabled for all)
            return senderName.equals(hostName) || source.hasPermissionLevel(2);
        }
    }

    private static String getConfigValue(String key) {
        return switch (key) {
            case "power" -> String.valueOf(ConfigHandler.config().power);
            case "penetration" -> String.valueOf(ConfigHandler.config().penetration);
            case "dropPercentage" -> String.valueOf(ConfigHandler.config().dropPercentage * 100);
            case "directionalOffset" -> String.valueOf(ConfigHandler.config().directionalOffset);
            default -> "Setting not found";
        };
    }

    private static boolean setConfigValue(String key, String value) {
        boolean success = true;
        try {
            if (!Objects.equals(value, "default")) {
                switch (key) {
                    case "power" -> ConfigHandler.config().power = Float.parseFloat(value);
                    case "penetration" -> ConfigHandler.config().penetration = Float.parseFloat(value);
                    case "dropPercentage" -> ConfigHandler.config().dropPercentage = Float.parseFloat(value) / 100F;
                    case "directionalOffset" -> ConfigHandler.config().directionalOffset = Integer.parseInt(value);
                    default -> success = false;
                }
            } else {
                switch (key) {
                    case "power" -> ConfigHandler.config().power = ConfigHandler.defaultConfig().power;
                    case "penetration" -> ConfigHandler.config().penetration = ConfigHandler.defaultConfig().penetration;
                    case "dropPercentage" -> ConfigHandler.config().dropPercentage = ConfigHandler.defaultConfig().dropPercentage;
                    case "directionalOffset" -> ConfigHandler.config().directionalOffset = ConfigHandler.defaultConfig().directionalOffset;
                    default -> success = false;
                }
            }
        } catch (NullPointerException | NumberFormatException e) {
            success = false;
        }
        return success;
    }
}

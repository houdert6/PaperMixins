package com.houdert6.papermixins;

import com.houdert6.papermixins.data.PaperMixinConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class PaperMixins extends JavaPlugin {
    @Override
    public void onEnable() {
        saveDefaultConfig(); // The config is used in the bootstrap but it's easier to have it generate from the main plugin class cuz bootstrap doesn't have many methods for interacting with configs
        // Some commands for viewing info about loaded mixins
        getServer().getCommandMap().register(getName().toLowerCase(), new Command("mixins") {
            @Override
            public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
                sender.sendMessage(Component.text("Server Mixins:").color(NamedTextColor.AQUA));
                for (PaperMixinConfig config : PaperMixinsBootstrap.getMixins()) {
                    sender.sendMessage(Component.text(" - ").color(NamedTextColor.DARK_GRAY).append(Component.text(config.name).color(NamedTextColor.BLUE)).clickEvent(ClickEvent.runCommand("mixinver " + config.name)));
                }
                return true;
            }
            @Override
            public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
                return List.of();
            }
        });
        getServer().getCommandMap().register(getName().toLowerCase(), new Command("mixinver") {
            @Override
            public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
                if (args.length != 1) {
                    sender.sendMessage(Component.text("/mixinver <mixin>").color(NamedTextColor.RED));
                    return true;
                }
                for (PaperMixinConfig config : PaperMixinsBootstrap.getMixins()) {
                    if (config.name.equalsIgnoreCase(args[0])) {
                        // Name and version
                        sender.sendMessage(Component.empty().append(Component.text(config.name).color(NamedTextColor.BLUE)).append(Component.text(" version ")).append(Component.text(config.version).color(NamedTextColor.BLUE)));
                        // Description
                        if (config.description != null && !config.description.isEmpty()) {
                            sender.sendMessage(Component.text(config.description));
                        }
                        // Author
                        if (config.author != null && !config.author.isEmpty()) {
                            sender.sendMessage(Component.text("Author: ").append(Component.text(config.author).color(NamedTextColor.BLUE)));
                        }
                        // Link
                        if (config.link != null && !config.link.isEmpty()) {
                            sender.sendMessage(Component.text("Website: ").append(Component.text(config.link).color(NamedTextColor.BLUE).clickEvent(ClickEvent.openUrl(config.link))));
                        }
                        // Related plugin
                        if (config.relatedPlugin != null && !config.relatedPlugin.isEmpty()) {
                            sender.sendMessage(Component.text("Related to plugin: ").append(Component.text(config.relatedPlugin).color(NamedTextColor.BLUE).clickEvent(ClickEvent.runCommand("ver " + config.relatedPlugin))));
                        }
                    }
                }
                return true;
            }
            @Override
            public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
                if (args.length == 1) {
                    List<String> mixins = PaperMixinsBootstrap.getMixins().stream().map(config -> config.name).toList();
                    List<String> completions = new ArrayList<>();
                    StringUtil.copyPartialMatches(args[args.length - 1], mixins, completions);
                    return completions;
                } else {
                    return List.of(); // No completions
                }
            }
        });
    }
}

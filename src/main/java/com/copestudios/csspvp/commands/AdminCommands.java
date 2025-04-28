package com.copestudios.csspvp.commands;

import com.copestudios.csspvp.CSSPVP;
import com.copestudios.csspvp.messages.MessageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AdminCommands implements CommandExecutor {
    private final CSSPVP plugin;

    public AdminCommands(CSSPVP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            // Open main GUI if sender is a player
            if (sender instanceof Player) {
                Player player = (Player) sender;
                plugin.getGuiManager().openMainMenu(player);
                return true;
            } else {
                sender.sendMessage(plugin.getMessageManager().getMessage(
                        MessageManager.GENERAL_PLAYER_ONLY
                ));
                return true;
            }
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
                return handleReload(sender);
            case "setspawn":
                return handleSetSpawn(sender);
            case "spawn":
                return handleSpawn(sender);
            case "help":
                return handleHelp(sender);
            default:
                sender.sendMessage(plugin.getMessageManager().getMessage(
                        MessageManager.GENERAL_INVALID_ARGS,
                        "usage", "/csspvp [reload|setspawn|spawn|help]"
                ));
                return true;
        }
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("csspvp.admin")) {
            sender.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.GENERAL_NO_PERMISSION
            ));
            return true;
        }

        // Reload configuration and messages
        plugin.getConfigManager().loadConfig();
        plugin.getMessageManager().loadMessages();

        sender.sendMessage(plugin.colorize("&aCSS PVP configuration and messages reloaded!"));
        return true;
    }

    private boolean handleSetSpawn(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.GENERAL_PLAYER_ONLY
            ));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("csspvp.admin")) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.GENERAL_NO_PERMISSION
            ));
            return true;
        }

        // Set general spawn location
        plugin.getConfigManager().setGeneralSpawn(player.getLocation());

        player.sendMessage(plugin.getMessageManager().getMessage(
                MessageManager.SPAWN_SET
        ));

        return true;
    }

    private boolean handleSpawn(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.GENERAL_PLAYER_ONLY
            ));
            return true;
        }

        Player player = (Player) sender;

        // Get general spawn location
        org.bukkit.Location spawnLocation = plugin.getConfigManager().getGeneralSpawn();

        if (spawnLocation == null) {
            player.sendMessage(plugin.colorize("&cGeneral spawn has not been set yet!"));
            return true;
        }

        // Teleport player to general spawn
        player.teleport(spawnLocation);

        player.sendMessage(plugin.getMessageManager().getMessage(
                MessageManager.SPAWN_TELEPORT
        ));

        return true;
    }

    private boolean handleHelp(CommandSender sender) {
        sender.sendMessage(plugin.colorize("&6=== CSSPVP Commands ==="));
        sender.sendMessage(plugin.colorize("&e/csspvp - &7Open main menu"));
        sender.sendMessage(plugin.colorize("&e/csspvp reload - &7Reload configuration"));
        sender.sendMessage(plugin.colorize("&e/csspvp setspawn - &7Set general spawn point"));
        sender.sendMessage(plugin.colorize("&e/csspvp spawn - &7Teleport to general spawn"));
        sender.sendMessage(plugin.colorize("&e/cssarena - &7Arena commands"));
        sender.sendMessage(plugin.colorize("&e/cssteam - &7Team commands"));
        sender.sendMessage(plugin.colorize("&e/cssduel - &7Duel commands"));

        return true;
    }
}
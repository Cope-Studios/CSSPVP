package com.copestudios.csspvp.commands;

import com.copestudios.csspvp.CSSPVP;
import com.copestudios.csspvp.arena.Arena;
import com.copestudios.csspvp.utils.MessageId;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CSRDCommand implements CommandExecutor, TabCompleter {

    private final CSSPVP plugin;

    public CSRDCommand(CSSPVP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            // Main command - show help
            sendHelpMessage(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "start":
                return handleStart(sender, args);
            case "leave":
                return handleLeave(sender);
            case "stop":
                return handleStop(sender, args);
            case "help":
                sendHelpMessage(sender);
                return true;
            default:
                // Check if arg[0] is an arena name
                Arena arena = plugin.getArenaManager().getArena(args[0]);
                if (arena != null) {
                    return handleStart(sender, args);
                }

                sender.sendMessage(MessageId.get("plugin.invalid-args", "usage", "/csrd <arena|start|leave|stop>"));
                return true;
        }
    }

    private boolean handleStart(CommandSender sender, String[] args) {
        if (!sender.hasPermission("csspvp.admin.random-duel")) {
            sender.sendMessage(MessageId.get("plugin.no-permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(MessageId.get("plugin.invalid-args", "usage", "/csrd <arena>"));
            return true;
        }

        String arenaName = args[0];
        if (args[0].equalsIgnoreCase("start") && args.length >= 2) {
            arenaName = args[1];
        }

        Arena arena = plugin.getArenaManager().getArena(arenaName);

        if (arena == null) {
            sender.sendMessage(MessageId.get("arena.not-found", "name", arenaName));
            return true;
        }

        plugin.getRandomDuelManager().startRandomDuel(arenaName);

        return true;
    }

    private boolean handleLeave(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageId.get("plugin.player-only"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("csspvp.random-duel")) {
            player.sendMessage(MessageId.get("plugin.no-permission"));
            return true;
        }

        if (!plugin.getRandomDuelManager().isInRandomDuel(player)) {
            player.sendMessage(MessageId.get("random-duel.not-in"));
            return true;
        }

        plugin.getRandomDuelManager().forfeitRandomDuel(player);

        return true;
    }

    private boolean handleStop(CommandSender sender, String[] args) {
        if (!sender.hasPermission("csspvp.admin.random-duel.stop")) {
            sender.sendMessage(MessageId.get("plugin.no-permission"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(MessageId.get("plugin.invalid-args", "usage", "/csrd stop <arena>"));
            return true;
        }

        String arenaName = args[1];
        Arena arena = plugin.getArenaManager().getArena(arenaName);

        if (arena == null) {
            sender.sendMessage(MessageId.get("arena.not-found", "name", arenaName));
            return true;
        }

        plugin.getRandomDuelManager().stopRandomDuel(arenaName);
        sender.sendMessage(MessageId.get("random-duel.stopped", "name", arenaName));

        return true;
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(MessageId.get("help.header"));

        if (sender.hasPermission("csspvp.admin.random-duel")) {
            sender.sendMessage(MessageId.get("help.csrd.arena"));
            sender.sendMessage(MessageId.get("help.csrd.start"));
        }

        sender.sendMessage(MessageId.get("help.csrd.leave"));

        if (sender.hasPermission("csspvp.admin.random-duel.stop")) {
            sender.sendMessage(MessageId.get("help.csrd.stop"));
        }

        sender.sendMessage(MessageId.get("help.footer"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // First argument - subcommands or arena names
            List<String> subCommands = Arrays.asList("start", "leave", "stop", "help");
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }

            // Add arena names
            for (String arenaName : plugin.getArenaManager().getArenaNames()) {
                if (arenaName.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(arenaName);
                }
            }
        } else if (args.length == 2) {
            // Second argument - arena name for start/stop
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("start") || subCommand.equals("stop")) {
                for (String arenaName : plugin.getArenaManager().getArenaNames()) {
                    if (arenaName.toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(arenaName);
                    }
                }
            }
        }

        return completions;
    }
}
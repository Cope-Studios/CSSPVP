package com.copestudios.csspvp.commands;

import com.copestudios.csspvp.CSSPVP;
import com.copestudios.csspvp.arena.Arena;
import com.copestudios.csspvp.utils.MessageId;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CSACommand implements CommandExecutor, TabCompleter {

    private final CSSPVP plugin;

    public CSACommand(CSSPVP plugin) {
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
            case "join":
                return handleJoin(sender, args);
            case "leave":
                return handleLeave(sender);
            case "setkit":
                return handleSetKit(sender, args);
            case "kit":
                return handleKit(sender, args);
            case "list":
                return handleList(sender);
            case "help":
                sendHelpMessage(sender);
                return true;
            default:
                sender.sendMessage(MessageId.get("plugin.invalid-args", "usage", "/csa <join|leave|setkit|kit|list>"));
                return true;
        }
    }

    private boolean handleJoin(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageId.get("plugin.player-only"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("csspvp.join")) {
            player.sendMessage(MessageId.get("plugin.no-permission"));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(MessageId.get("plugin.invalid-args", "usage", "/csa join <arena>"));
            return true;
        }

        String arenaName = args[1];
        plugin.getArenaManager().joinArena(player, arenaName);

        return true;
    }

    private boolean handleLeave(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageId.get("plugin.player-only"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("csspvp.leave")) {
            player.sendMessage(MessageId.get("plugin.no-permission"));
            return true;
        }

        plugin.getArenaManager().leaveArena(player);

        return true;
    }

    private boolean handleSetKit(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageId.get("plugin.player-only"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("csspvp.admin.setkit")) {
            player.sendMessage(MessageId.get("plugin.no-permission"));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(MessageId.get("plugin.invalid-args", "usage", "/csa setkit <arena>"));
            return true;
        }

        String arenaName = args[1];
        Arena arena = plugin.getArenaManager().getArena(arenaName);

        if (arena == null) {
            player.sendMessage(MessageId.get("arena.not-found", "name", arenaName));
            return true;
        }

        // Save player's inventory as kit
        plugin.getKitManager().setArenaKit(arenaName, player.getInventory().getContents());

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("name", arenaName);
        player.sendMessage(MessageId.get("kit.set", placeholders));

        return true;
    }

    private boolean handleKit(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(MessageId.get("plugin.invalid-args", "usage", "/csa kit <arena> [all|player]"));
            return true;
        }

        String arenaName = args[1];
        Arena arena = plugin.getArenaManager().getArena(arenaName);

        if (arena == null) {
            sender.sendMessage(MessageId.get("arena.not-found", "name", arenaName));
            return true;
        }

        if (args.length >= 3 && sender.hasPermission("csspvp.admin.kit.others")) {
            // Admin giving kit to all or specific player
            String target = args[2];

            if (target.equalsIgnoreCase("all")) {
                // Give kit to all players in arena
                plugin.getKitManager().giveKitToAll(arenaName);
                sender.sendMessage(MessageId.get("kit.given-all", "name", arenaName));
            } else {
                // Give kit to specific player
                Player targetPlayer = Bukkit.getPlayer(target);
                if (targetPlayer == null) {
                    sender.sendMessage(MessageId.get("player.not-found", "player", target));
                    return true;
                }

                if (!plugin.getArenaManager().isInArena(targetPlayer)) {
                    sender.sendMessage(MessageId.get("player.not-in-arena", "player", target));
                    return true;
                }

                plugin.getKitManager().giveKit(targetPlayer, arenaName);

                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("name", arenaName);
                placeholders.put("player", targetPlayer.getName());
                sender.sendMessage(MessageId.get("kit.given-other", placeholders));
            }

            return true;
        } else if (sender instanceof Player) {
            // Player getting kit for themselves
            Player player = (Player) sender;

            if (!player.hasPermission("csspvp.kit")) {
                player.sendMessage(MessageId.get("plugin.no-permission"));
                return true;
            }

            if (!plugin.getArenaManager().isInArena(player)) {
                player.sendMessage(MessageId.get("arena.not-in"));
                return true;
            }

            if (!plugin.getArenaManager().getPlayerArena(player).getName().equalsIgnoreCase(arenaName)) {
                player.sendMessage(MessageId.get("arena.not-in-specific", "name", arenaName));
                return true;
            }

            plugin.getKitManager().giveKit(player, arenaName);
        } else {
            sender.sendMessage(MessageId.get("plugin.player-only"));
        }

        return true;
    }

    private boolean handleList(CommandSender sender) {
        if (!sender.hasPermission("csspvp.list")) {
            sender.sendMessage(MessageId.get("plugin.no-permission"));
            return true;
        }

        sender.sendMessage(MessageId.get("arena.list.header"));

        for (String arenaName : plugin.getArenaManager().getArenaNames()) {
            Arena arena = plugin.getArenaManager().getArena(arenaName);
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("name", arenaName);
            placeholders.put("players", String.valueOf(arena.getPlayerCount()));
            placeholders.put("max", String.valueOf(arena.getSettings().getMaxPlayers()));
            sender.sendMessage(MessageId.get("arena.list.entry", placeholders));
        }

        sender.sendMessage(MessageId.get("arena.list.footer"));

        return true;
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(MessageId.get("help.header"));
        sender.sendMessage(MessageId.get("help.csa.join"));
        sender.sendMessage(MessageId.get("help.csa.leave"));

        if (sender.hasPermission("csspvp.admin.setkit")) {
            sender.sendMessage(MessageId.get("help.csa.setkit"));
        }

        sender.sendMessage(MessageId.get("help.csa.kit"));
        sender.sendMessage(MessageId.get("help.csa.list"));
        sender.sendMessage(MessageId.get("help.footer"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // First argument - subcommands
            List<String> subCommands = Arrays.asList("join", "leave", "setkit", "kit", "list", "help");
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length == 2) {
            // Second argument - arena name
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("join") || subCommand.equals("setkit") || subCommand.equals("kit")) {
                for (String arenaName : plugin.getArenaManager().getArenaNames()) {
                    if (arenaName.startsWith(args[1].toLowerCase())) {
                        completions.add(arenaName);
                    }
                }
            }
        } else if (args.length == 3) {
            // Third argument - player name or "all" for kit command
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("kit") && sender.hasPermission("csspvp.admin.kit.others")) {
                if ("all".startsWith(args[2].toLowerCase())) {
                    completions.add("all");
                }

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase().startsWith(args[2].toLowerCase())) {
                        completions.add(player.getName());
                    }
                }
            }
        }

        return completions;
    }
}
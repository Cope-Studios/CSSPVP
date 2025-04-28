package com.copestudios.csspvp.commands;

import com.copestudios.csspvp.CSSPVP;
import com.copestudios.csspvp.arena.Arena;
import com.copestudios.csspvp.duel.DuelManager;
import com.copestudios.csspvp.messages.MessageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DuelCommands implements CommandExecutor {
    private final CSSPVP plugin;
    private final DuelManager duelManager;

    public DuelCommands(CSSPVP plugin) {
        this.plugin = plugin;
        this.duelManager = new DuelManager(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.GENERAL_PLAYER_ONLY
            ));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            // Show help
            displayHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "challenge":
                return handleChallenge(player, args);
            case "accept":
                return handleAccept(player);
            case "decline":
                return handleDecline(player);
            case "csrduel":
            case "r":
            case "random":
                return handleRandomDuel(player, args);
            default:
                // Check if it's a player name for direct challenge
                Player target = plugin.getServer().getPlayer(args[0]);
                if (target != null && args.length > 1) {
                    String[] newArgs = new String[args.length + 1];
                    newArgs[0] = "challenge";
                    System.arraycopy(args, 0, newArgs, 1, args.length);
                    return handleChallenge(player, newArgs);
                }

                // Otherwise show help
                displayHelp(player);
                return true;
        }
    }

    private void displayHelp(Player player) {
        player.sendMessage(plugin.colorize("&6=== CSSPVP Duel Commands ==="));
        player.sendMessage(plugin.colorize("&e/cssduel challenge <player> <arena> - &7Challenge a player to a duel"));
        player.sendMessage(plugin.colorize("&e/cssduel <player> <arena> - &7Short for challenge command"));
        player.sendMessage(plugin.colorize("&e/cssduel accept - &7Accept a pending duel request"));
        player.sendMessage(plugin.colorize("&e/cssduel decline - &7Decline a pending duel request"));
        player.sendMessage(plugin.colorize("&e/cssduel random <arena> - &7Start a random duel (OP only)"));
        player.sendMessage(plugin.colorize("&e/cssrduel <arena> - &7Start a random duel between 2 random players (OP only)"));
    }

    private boolean handleChallenge(Player player, String[] args) {
        if (!player.hasPermission("csspvp.duel")) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.GENERAL_NO_PERMISSION
            ));
            return true;
        }

        if (args.length < 3) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.GENERAL_INVALID_ARGS,
                    "usage", "/cssduel challenge <player> <arena>"
            ));
            return true;
        }

        // Get target player
        Player target = plugin.getServer().getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.GENERAL_PLAYER_NOT_FOUND,
                    "player", args[1]
            ));
            return true;
        }

        // Check if challenging self
        if (target.equals(player)) {
            player.sendMessage(plugin.colorize("&cYou cannot duel yourself!"));
            return true;
        }

        // Get arena
        String arenaName = args[2];
        Arena arena = plugin.getArenaManager().getArena(arenaName);

        if (arena == null) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.ARENA_NOT_FOUND,
                    "name", arenaName
            ));
            return true;
        }

        // Check if arena is setup
        if (!arena.isSetup()) {
            player.sendMessage(plugin.colorize("&cArena " + arenaName + " is not fully set up yet!"));
            return true;
        }

        // Check if arena is available
        if (arena.getState() != Arena.ArenaState.INACTIVE) {
            player.sendMessage(plugin.colorize("&cArena " + arenaName + " is currently in use!"));
            return true;
        }

        // Send duel request
        if (duelManager.sendDuelRequest(player, target, arenaName)) {
            // Success - messages sent in DuelManager
        } else {
            // Failed - check if there's an existing request
            if (duelManager.hasOutgoingRequest(player, target)) {
                player.sendMessage(plugin.getMessageManager().getMessage(
                        MessageManager.DUEL_ALREADY_SENT,
                        "player", target.getName()
                ));
            } else {
                player.sendMessage(plugin.colorize("&cFailed to send duel request. Players might already be in an arena."));
            }
        }

        return true;
    }

    private boolean handleAccept(Player player) {
        if (!player.hasPermission("csspvp.duel")) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.GENERAL_NO_PERMISSION
            ));
            return true;
        }

        // Check if has pending request
        if (!duelManager.hasIncomingRequest(player)) {
            player.sendMessage(plugin.colorize("&cYou don't have any pending duel requests!"));
            return true;
        }

        // Accept request
        if (duelManager.acceptDuelRequest(player)) {
            // Success - messages and teleportation handled in DuelManager
        } else {
            player.sendMessage(plugin.colorize("&cFailed to accept duel request. The request may have expired or the arena is no longer available."));
        }

        return true;
    }

    private boolean handleDecline(Player player) {
        if (!player.hasPermission("csspvp.duel")) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.GENERAL_NO_PERMISSION
            ));
            return true;
        }

        // Check if has pending request
        if (!duelManager.hasIncomingRequest(player)) {
            player.sendMessage(plugin.colorize("&cYou don't have any pending duel requests!"));
            return true;
        }

        // Decline request
        if (duelManager.declineDuelRequest(player)) {
            // Success - messages handled in DuelManager
        } else {
            player.sendMessage(plugin.colorize("&cFailed to decline duel request. The request may have already expired."));
        }

        return true;
    }

    private boolean handleRandomDuel(Player player, String[] args) {
        // For /cssrduel command - random duel with random players (command issuer excluded)
        if (args[0].equalsIgnoreCase("csrduel") || args[0].equalsIgnoreCase("r")) {
            if (!player.isOp()) {
                player.sendMessage(plugin.getMessageManager().getMessage(
                        MessageManager.GENERAL_NO_PERMISSION
                ));
                return true;
            }

            if (args.length < 2) {
                player.sendMessage(plugin.getMessageManager().getMessage(
                        MessageManager.GENERAL_INVALID_ARGS,
                        "usage", "/cssrduel <arena>"
                ));
                return true;
            }

            String arenaName = args[1];

            // Force random duel between 2 random players (excluding command sender)
            if (duelManager.startRandomDuel(arenaName, player)) {
                player.sendMessage(plugin.colorize("&aStarted random duel in arena " + arenaName + "!"));
            } else {
                player.sendMessage(plugin.colorize("&cFailed to start random duel. Not enough eligible players or arena not available."));
            }

            return true;
        }

        // Regular random duel command
        if (!player.hasPermission("csspvp.admin")) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.GENERAL_NO_PERMISSION
            ));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.GENERAL_INVALID_ARGS,
                    "usage", "/cssduel random <arena>"
            ));
            return true;
        }

        String arenaName = args[1];

        // Start random duel
        if (duelManager.startRandomDuel(arenaName)) {
            player.sendMessage(plugin.colorize("&aStarted random duel in arena " + arenaName + "!"));
        } else {
            player.sendMessage(plugin.colorize("&cFailed to start random duel. Not enough eligible players or arena not available."));
        }

        return true;
    }
}
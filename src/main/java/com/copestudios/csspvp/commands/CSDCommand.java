package com.copestudios.csspvp.commands;

import com.copestudios.csspvp.CSSPVP;
import com.copestudios.csspvp.duel.Duel;
import com.copestudios.csspvp.duel.DuelZone;
import com.copestudios.csspvp.duel.ZoneSettings;
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
import java.util.UUID;

public class CSDCommand implements CommandExecutor, TabCompleter {

    private final CSSPVP plugin;
    private final Map<UUID, String> creatingZone;
    private final Map<UUID, List<org.bukkit.Location>> zoneCorners;
    private final Map<UUID, String> settingUpZone;
    private final Map<UUID, org.bukkit.Location> spawnPoint1;

    public CSDCommand(CSSPVP plugin) {
        this.plugin = plugin;
        this.creatingZone = new HashMap<>();
        this.zoneCorners = new HashMap<>();
        this.settingUpZone = new HashMap<>();
        this.spawnPoint1 = new HashMap<>();
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
            case "create":
                return handleCreate(sender, args);
            case "setup":
                return handleSetup(sender, args);
            case "accept":
                return handleAccept(sender);
            case "leave":
                return handleLeave(sender);
            case "stop":
                return handleStop(sender, args);
            case "help":
                sendHelpMessage(sender);
                return true;
            default:
                // Check if arg[0] is a player name for duel request
                Player target = Bukkit.getPlayer(args[0]);
                if (target != null) {
                    return handleDuelRequest(sender, target);
                }

                sender.sendMessage(MessageId.get("plugin.invalid-args", "usage", "/csd <player|create|setup|accept|leave|stop>"));
                return true;
        }
    }

    private boolean handleCreate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageId.get("plugin.player-only"));
            return true;
        }

        if (!sender.hasPermission("csspvp.admin.create.zone")) {
            sender.sendMessage(MessageId.get("plugin.no-permission"));
            return true;
        }

        Player player = (Player) sender;
        UUID playerId = player.getUniqueId();

        if (args.length == 1) {
            // Start zone creation process
            if (creatingZone.containsKey(playerId) || zoneCorners.containsKey(playerId)) {
                player.sendMessage(MessageId.get("zone.create.already"));
                return true;
            }

            // Add player to creation mode
            creatingZone.put(playerId, null);
            player.sendMessage(MessageId.get("zone.create.start"));
            return true;
        }

        if (args.length == 2 && args[1].equalsIgnoreCase("cancel")) {
            // Cancel zone creation
            creatingZone.remove(playerId);
            zoneCorners.remove(playerId);
            spawnPoint1.remove(playerId);
            player.sendMessage(MessageId.get("zone.create.canceled"));
            return true;
        }

        return true;
    }

    private boolean handleSetup(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageId.get("plugin.player-only"));
            return true;
        }

        if (!sender.hasPermission("csspvp.admin.setup.zone")) {
            sender.sendMessage(MessageId.get("plugin.no-permission"));
            return true;
        }

        Player player = (Player) sender;
        UUID playerId = player.getUniqueId();

        if (args.length < 2) {
            player.sendMessage(MessageId.get("plugin.invalid-args", "usage", "/csd setup <zone>"));
            return true;
        }

        String zoneName = args[1];
        DuelZone zone = plugin.getDuelManager().getZone(zoneName);

        if (zone == null) {
            player.sendMessage(MessageId.get("zone.not-found", "name", zoneName));
            return true;
        }

        // Start setup process
        settingUpZone.put(playerId, zoneName);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("name", zoneName);
        player.sendMessage(MessageId.get("zone.setup.start", placeholders));
        player.sendMessage(MessageId.get("zone.setup.drop-items"));

        return true;
    }

    private boolean handleDuelRequest(CommandSender sender, Player target) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageId.get("plugin.player-only"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("csspvp.duel")) {
            player.sendMessage(MessageId.get("plugin.no-permission"));
            return true;
        }

        if (player.equals(target)) {
            player.sendMessage(MessageId.get("duel.request.self"));
            return true;
        }

        plugin.getDuelManager().requestDuel(player, target);

        return true;
    }

    private boolean handleAccept(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageId.get("plugin.player-only"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("csspvp.duel")) {
            player.sendMessage(MessageId.get("plugin.no-permission"));
            return true;
        }

        plugin.getDuelManager().acceptDuel(player);

        return true;
    }

    private boolean handleLeave(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageId.get("plugin.player-only"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("csspvp.duel")) {
            player.sendMessage(MessageId.get("plugin.no-permission"));
            return true;
        }

        if (!plugin.getDuelManager().isInDuel(player)) {
            player.sendMessage(MessageId.get("duel.not-in"));
            return true;
        }

        plugin.getDuelManager().forfeitDuel(player);

        return true;
    }

    private boolean handleStop(CommandSender sender, String[] args) {
        if (!sender.hasPermission("csspvp.admin.duel.stop")) {
            sender.sendMessage(MessageId.get("plugin.no-permission"));
            return true;
        }

        if (args.length < 2) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                Duel duel = plugin.getDuelManager().getPlayerDuel(player);

                if (duel != null) {
                    plugin.getDuelManager().stopDuel(duel, false);
                    return true;
                }
            }

            sender.sendMessage(MessageId.get("plugin.invalid-args", "usage", "/csd stop <player>"));
            return true;
        }

        String playerName = args[1];
        Player target = Bukkit.getPlayer(playerName);

        if (target == null) {
            sender.sendMessage(MessageId.get("player.not-found", "player", playerName));
            return true;
        }

        Duel duel = plugin.getDuelManager().getPlayerDuel(target);

        if (duel == null) {
            sender.sendMessage(MessageId.get("player.not-in-duel", "player", playerName));
            return true;
        }

        plugin.getDuelManager().stopDuel(duel, false);

        return true;
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(MessageId.get("help.header"));
        sender.sendMessage(MessageId.get("help.csd.player"));
        sender.sendMessage(MessageId.get("help.csd.accept"));
        sender.sendMessage(MessageId.get("help.csd.leave"));

        if (sender.hasPermission("csspvp.admin.create.zone")) {
            sender.sendMessage(MessageId.get("help.csd.create"));
        }

        if (sender.hasPermission("csspvp.admin.setup.zone")) {
            sender.sendMessage(MessageId.get("help.csd.setup"));
        }

        if (sender.hasPermission("csspvp.admin.duel.stop")) {
            sender.sendMessage(MessageId.get("help.csd.stop"));
        }

        sender.sendMessage(MessageId.get("help.footer"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // First argument - subcommands or player names
            List<String> subCommands = Arrays.asList("create", "setup", "accept", "leave", "stop", "help");
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }

            // Add online players for duel requests
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(player.getName());
                }
            }
        } else if (args.length == 2) {
            // Second argument - zone name for setup, player name for stop, or "cancel" for create
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("setup")) {
                for (String zoneName : plugin.getDuelManager().getZoneNames()) {
                    if (zoneName.startsWith(args[1].toLowerCase())) {
                        completions.add(zoneName);
                    }
                }
            } else if (subCommand.equals("stop")) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(player.getName());
                    }
                }
            } else if (subCommand.equals("create")) {
                if ("cancel".startsWith(args[1].toLowerCase())) {
                    completions.add("cancel");
                }
            }
        }

        return completions;
    }

    // Methods to be called from ChatListener for handling zone creation chat inputs
    public boolean isCreatingZone(UUID playerId) {
        return creatingZone.containsKey(playerId);
    }

    public boolean hasZoneName(UUID playerId) {
        return creatingZone.containsKey(playerId) && creatingZone.get(playerId) != null;
    }

    public void setZoneName(UUID playerId, String zoneName) {
        creatingZone.put(playerId, zoneName);
        zoneCorners.put(playerId, new ArrayList<>());
    }

    public String getZoneName(UUID playerId) {
        return creatingZone.get(playerId);
    }

    public void addZoneCorner(UUID playerId, org.bukkit.Location location) {
        if (zoneCorners.containsKey(playerId)) {
            zoneCorners.get(playerId).add(location);
        }
    }

    public int getZoneCornerCount(UUID playerId) {
        return zoneCorners.containsKey(playerId) ? zoneCorners.get(playerId).size() : 0;
    }

    public void setSpawnPoint1(UUID playerId, org.bukkit.Location location) {
        spawnPoint1.put(playerId, location);
    }

    public boolean hasSpawnPoint1(UUID playerId) {
        return spawnPoint1.containsKey(playerId);
    }

    public boolean completeZoneCreation(UUID playerId, org.bukkit.Location spawnLocation2) {
        String zoneName = creatingZone.get(playerId);
        List<org.bukkit.Location> corners = zoneCorners.get(playerId);
        org.bukkit.Location spawn1 = spawnPoint1.get(playerId);

        boolean success = plugin.getDuelManager().createZone(zoneName, corners, spawn1, spawnLocation2);

        // Clean up
        creatingZone.remove(playerId);
        zoneCorners.remove(playerId);
        spawnPoint1.remove(playerId);

        return success;
    }

    public boolean isSettingUpZone(UUID playerId) {
        return settingUpZone.containsKey(playerId);
    }

    public String getSetupZoneName(UUID playerId) {
        return settingUpZone.get(playerId);
    }

    public void completeZoneSetup(UUID playerId) {
        settingUpZone.remove(playerId);
    }
}
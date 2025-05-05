package com.copestudios.csspvp.commands;

import com.copestudios.csspvp.CSSPVP;
import com.copestudios.csspvp.arena.Arena;
import com.copestudios.csspvp.arena.ArenaSettings;
import com.copestudios.csspvp.gui.ArenaSettingsGUI;
import com.copestudios.csspvp.utils.MessageId;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

public class CSPCommand implements CommandExecutor, TabCompleter {

    private final CSSPVP plugin;
    private final Map<UUID, String> creatingArena;
    private final Map<UUID, List<org.bukkit.Location>> arenaCorners;
    private final Map<UUID, String> settingUpArena;

    public CSPCommand(CSSPVP plugin) {
        this.plugin = plugin;
        this.creatingArena = new HashMap<>();
        this.arenaCorners = new HashMap<>();
        this.settingUpArena = new HashMap<>();
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
            case "setlobby":
                return handleSetLobby(sender);
            case "create":
                return handleCreate(sender, args);
            case "setup":
                return handleSetup(sender, args);
            case "gui":
                return handleGUI(sender, args);
            case "help":
                sendHelpMessage(sender);
                return true;
            default:
                sender.sendMessage(MessageId.get("plugin.invalid-args", "usage", "/csp <setlobby|create|setup|gui>"));
                return true;
        }
    }

    private boolean handleSetLobby(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageId.get("plugin.player-only"));
            return true;
        }

        if (!sender.hasPermission("csspvp.admin.setlobby")) {
            sender.sendMessage(MessageId.get("plugin.no-permission"));
            return true;
        }

        Player player = (Player) sender;
        plugin.getConfigManager().setLobbyLocation(player.getLocation());
        player.sendMessage(MessageId.get("lobby.set"));
        return true;
    }

    private boolean handleCreate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageId.get("plugin.player-only"));
            return true;
        }

        if (!sender.hasPermission("csspvp.admin.create")) {
            sender.sendMessage(MessageId.get("plugin.no-permission"));
            return true;
        }

        Player player = (Player) sender;
        UUID playerId = player.getUniqueId();

        if (args.length == 1) {
            // Start arena creation process
            if (creatingArena.containsKey(playerId) || arenaCorners.containsKey(playerId)) {
                player.sendMessage(MessageId.get("arena.create.already"));
                return true;
            }

            // Add player to creation mode
            creatingArena.put(playerId, null);
            player.sendMessage(MessageId.get("arena.create.start"));
            return true;
        }

        if (args.length == 2 && args[1].equalsIgnoreCase("cancel")) {
            // Cancel arena creation
            creatingArena.remove(playerId);
            arenaCorners.remove(playerId);
            player.sendMessage(MessageId.get("arena.create.canceled"));
            return true;
        }

        return true;
    }

    private boolean handleSetup(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageId.get("plugin.player-only"));
            return true;
        }

        if (!sender.hasPermission("csspvp.admin.setup")) {
            sender.sendMessage(MessageId.get("plugin.no-permission"));
            return true;
        }

        Player player = (Player) sender;
        UUID playerId = player.getUniqueId();

        if (args.length < 2) {
            player.sendMessage(MessageId.get("plugin.invalid-args", "usage", "/csp setup <arena>"));
            return true;
        }

        String arenaName = args[1];
        Arena arena = plugin.getArenaManager().getArena(arenaName);

        if (arena == null) {
            player.sendMessage(MessageId.get("arena.not-found", "name", arenaName));
            return true;
        }

        // Start setup process
        settingUpArena.put(playerId, arenaName);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("name", arenaName);
        player.sendMessage(MessageId.get("arena.setup.start", placeholders));
        player.sendMessage(MessageId.get("arena.setup.lives"));

        return true;
    }

    private boolean handleGUI(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageId.get("plugin.player-only"));
            return true;
        }

        if (!sender.hasPermission("csspvp.admin.gui")) {
            sender.sendMessage(MessageId.get("plugin.no-permission"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage(MessageId.get("plugin.invalid-args", "usage", "/csp gui <arena>"));
            return true;
        }

        String arenaName = args[1];
        Arena arena = plugin.getArenaManager().getArena(arenaName);

        if (arena == null) {
            player.sendMessage(MessageId.get("arena.not-found", "name", arenaName));
            return true;
        }

        // Open GUI for arena
        new ArenaSettingsGUI(plugin, player, arena).open();

        return true;
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(MessageId.get("help.header"));
        sender.sendMessage(MessageId.get("help.csp.setlobby"));
        sender.sendMessage(MessageId.get("help.csp.create"));
        sender.sendMessage(MessageId.get("help.csp.setup"));
        sender.sendMessage(MessageId.get("help.csp.gui"));
        sender.sendMessage(MessageId.get("help.footer"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // First argument - subcommands
            List<String> subCommands = Arrays.asList("setlobby", "create", "setup", "gui", "help");
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length == 2) {
            // Second argument - arena name for setup/gui
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("setup") || subCommand.equals("gui")) {
                for (String arenaName : plugin.getArenaManager().getArenaNames()) {
                    if (arenaName.startsWith(args[1].toLowerCase())) {
                        completions.add(arenaName);
                    }
                }
            } else if (subCommand.equals("create")) {
                completions.add("cancel");
            }
        }

        return completions;
    }

    // Methods to be called from ChatListener for handling arena creation chat inputs
    public boolean isCreatingArena(UUID playerId) {
        return creatingArena.containsKey(playerId);
    }

    public boolean hasArenaName(UUID playerId) {
        return creatingArena.containsKey(playerId) && creatingArena.get(playerId) != null;
    }

    public void setArenaName(UUID playerId, String arenaName) {
        creatingArena.put(playerId, arenaName);
        arenaCorners.put(playerId, new ArrayList<>());
    }

    public String getArenaName(UUID playerId) {
        return creatingArena.get(playerId);
    }

    public void addArenaCorner(UUID playerId, org.bukkit.Location location) {
        if (arenaCorners.containsKey(playerId)) {
            arenaCorners.get(playerId).add(location);
        }
    }

    public int getArenaCornerCount(UUID playerId) {
        return arenaCorners.containsKey(playerId) ? arenaCorners.get(playerId).size() : 0;
    }

    public boolean completeArenaCreation(UUID playerId, org.bukkit.Location spawnLocation) {
        String arenaName = creatingArena.get(playerId);
        List<org.bukkit.Location> corners = arenaCorners.get(playerId);

        boolean success = plugin.getArenaManager().createArena(arenaName, corners, spawnLocation);

        // Clean up
        creatingArena.remove(playerId);
        arenaCorners.remove(playerId);

        return success;
    }

    public boolean isSettingUpArena(UUID playerId) {
        return settingUpArena.containsKey(playerId);
    }

    public String getSetupArenaName(UUID playerId) {
        return settingUpArena.get(playerId);
    }

    public void completeArenaSetup(UUID playerId) {
        settingUpArena.remove(playerId);
    }
}
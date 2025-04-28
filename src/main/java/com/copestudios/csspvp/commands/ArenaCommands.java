package com.copestudios.csspvp.commands;

import com.copestudios.csspvp.CSSPVP;
import com.copestudios.csspvp.arena.Arena;
import com.copestudios.csspvp.messages.MessageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArenaCommands implements CommandExecutor {
    private final CSSPVP plugin;

    public ArenaCommands(CSSPVP plugin) {
        this.plugin = plugin;
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
            // Open arena menu
            plugin.getGuiManager().openArenaMenu(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
                return handleCreate(player, args);
            case "delete":
                return handleDelete(player, args);
            case "setspawn":
                return handleSetSpawn(player, args);
            case "setcorner1":
                return handleSetCorner1(player, args);
            case "setcorner2":
                return handleSetCorner2(player, args);
            case "setlives":
                return handleSetLives(player, args);
            case "setteamsize":
                return handleSetTeamSize(player, args);
            case "join":
                return handleJoin(player, args);
            case "leave":
                return handleLeave(player);
            case "start":
                return handleStart(player, args);
            case "info":
                return handleInfo(player, args);
            case "list":
                return handleList(player);
            default:
                player.sendMessage(plugin.getMessageManager().getMessage(
                        MessageManager.GENERAL_INVALID_ARGS,
                        "usage", "/cssarena [create|delete|setspawn|setcorner1|setcorner2|setlives|setteamsize|join|leave|start|info|list]"
                ));
                return true;
        }
    }

    private boolean handleCreate(Player player, String[] args) {
        if (!player.hasPermission("csspvp.arena.create")) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.GENERAL_NO_PERMISSION
            ));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.GENERAL_INVALID_ARGS,
                    "usage", "/cssarena create <name>"
            ));
            return true;
        }

        String arenaName = args[1];

        if (plugin.getArenaManager().createArena(arenaName)) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.ARENA_CREATED,
                    "name", arenaName
            ));
        } else {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.ARENA_ALREADY_EXISTS,
                    "name", arenaName
            ));
        }

        return true;
    }

    private boolean handleDelete(Player player, String[] args) {
        if (!player.hasPermission("csspvp.arena.delete")) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.GENERAL_NO_PERMISSION
            ));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.GENERAL_INVALID_ARGS,
                    "usage", "/cssarena delete <name>"
            ));
            return true;
        }

        String arenaName = args[1];

        if (plugin.getArenaManager().deleteArena(arenaName)) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.ARENA_DELETED,
                    "name", arenaName
            ));
        } else {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.ARENA_NOT_FOUND,
                    "name", arenaName
            ));
        }

        return true;
    }

    private boolean handleSetSpawn(Player player, String[] args) {
        if (!player.hasPermission("csspvp.arena.create")) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.GENERAL_NO_PERMISSION
            ));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.GENERAL_INVALID_ARGS,
                    "usage", "/cssarena setspawn <name>"
            ));
            return true;
        }

        String arenaName = args[1];
        Arena arena = plugin.getArenaManager().getArena(arenaName);

        if (arena == null) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.ARENA_NOT_FOUND,
                    "name", arenaName
            ));
            return true;
        }

        arena.setSpawnPoint(player.getLocation());
        plugin.getArenaManager().saveArenas();

        player.sendMessage(plugin.getMessageManager().getMessage(
                MessageManager.ARENA_SPAWN_SET,
                "name", arenaName
        ));

        return true;
    }

    private boolean handleSetCorner1(Player player, String[] args) {
        if (!player.hasPermission("csspvp.arena.create")) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.GENERAL_NO_PERMISSION
            ));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.GENERAL_INVALID_ARGS,
                    "usage", "/cssarena setcorner1 <name>"
            ));
            return true;
        }

        String arenaName = args[1];
        Arena arena = plugin.getArenaManager().getArena(arenaName);

        if (arena == null) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.ARENA_NOT_FOUND,
                    "name", arenaName
            ));
            return true;
        }

        arena.setCorner1(player.getLocation());
        plugin.getArenaManager().saveArenas();

        player.sendMessage(plugin.getMessageManager().getMessage(
                MessageManager.ARENA_CORNER_SET,
                "name", arenaName,
                "number", "1"
        ));

        return true;
    }

    private boolean handleSetCorner2(Player player, String[] args) {
        if (!player.hasPermission("csspvp.arena.create")) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.GENERAL_NO_PERMISSION
            ));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.GENERAL_INVALID_ARGS,
                    "usage", "/cssarena setcorner2 <name>"
            ));
            return true;
        }

        String arenaName = args[1];
        Arena arena = plugin.getArenaManager().getArena(arenaName);

        if (arena == null) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.ARENA_NOT_FOUND,
                    "name", arenaName
            ));
            return true;
        }

        arena.setCorner2(player.getLocation());
        plugin.getArenaManager().saveArenas();

        player.sendMessage(plugin.getMessageManager().getMessage(
                MessageManager.ARENA_CORNER_SET,
                "name", arenaName,
                "number", "2"
        ));

        return true;
    }

    private boolean handleSetLives(Player player, String[] args) {
        if (!player.hasPermission("csspvp.arena.create")) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.GENERAL_NO_PERMISSION
            ));
            return true;
        }

        if (args.length < 3) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.GENERAL_INVALID_ARGS,
                    "usage", "/cssarena setlives <name> <lives>"
            ));
            return true;
        }

        String arenaName = args[1];
        Arena arena = plugin.getArenaManager().getArena(arenaName);

        if (arena == null) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.ARENA_NOT_FOUND,
                    "name", arenaName
            ));
            return true;
        }

        int lives;
        try {
            lives = Integer.parseInt(args[2]);
            if (lives < 1) {
                player.sendMessage(plugin.colorize("&cLives must be at least 1!"));
                return true;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(plugin.colorize("&cLives must be a number!"));
            return true;
        }

        arena.setMaxLives(lives);
        plugin.getArenaManager().saveArenas();

        player.sendMessage(plugin.colorize("&aLives for arena " + arenaName + " set to " + lives + "!"));

        return true;
    }

    private boolean handleSetTeamSize(Player player, String[] args) {
        if (!player.hasPermission("csspvp.arena.create")) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.GENERAL_NO_PERMISSION
            ));
            return true;
        }

        if (args.length < 3) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.GENERAL_INVALID_ARGS,
                    "usage", "/cssarena setteamsize <name> <size>"
            ));
            return true;
        }

        String arenaName = args[1];
        Arena arena = plugin.getArenaManager().getArena(arenaName);

        if (arena == null) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.ARENA_NOT_FOUND,
                    "name", arenaName
            ));
            return true;
        }

        int teamSize;
        try {
            teamSize = Integer.parseInt(args[2]);
            if (teamSize < 1) {
                player.sendMessage(plugin.colorize("&cTeam size must be at least 1!"));
                return true;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(plugin.colorize("&cTeam size must be a number!"));
            return true;
        }

        arena.setTeamSize(teamSize);
        plugin.getArenaManager().saveArenas();

        player.sendMessage(plugin.colorize("&aTeam size for arena " + arenaName + " set to " + teamSize + "!"));

        return true;
    }

    private boolean handleJoin(Player player, String[] args) {
        if (!player.hasPermission("csspvp.arena.join")) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.GENERAL_NO_PERMISSION
            ));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.GENERAL_INVALID_ARGS,
                    "usage", "/cssarena join <name>"
            ));
            return true;
        }

        // Check if already in an arena
        if (plugin.getArenaManager().isPlayerInArena(player)) {
            player.sendMessage(plugin.colorize("&cYou are already in an arena! Use /cssarena leave first."));
            return true;
        }

        String arenaName = args[1];
        Arena arena = plugin.getArenaManager().getArena(arenaName);

        if (arena == null) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.ARENA_NOT_FOUND,
                    "name", arenaName
            ));
            return true;
        }

        // Check if arena is set up
        if (!arena.isSetup()) {
            player.sendMessage(plugin.colorize("&cArena is not fully set up yet!"));
            return true;
        }

        // Try to add to arena
        if (arena.addPlayer(player)) {
            // Success - already sent join message in Arena class
        } else {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.ARENA_FULL,
                    "name", arenaName
            ));
        }

        return true;
    }

    private boolean handleLeave(Player player) {
        Arena arena = plugin.getArenaManager().getPlayerArena(player);

        if (arena == null) {
            player.sendMessage(plugin.colorize("&cYou are not in any arena!"));
            return true;
        }

        arena.removePlayer(player);

        // Teleport to general spawn
        org.bukkit.Location spawnLocation = plugin.getConfigManager().getGeneralSpawn();
        if (spawnLocation != null) {
            player.teleport(spawnLocation);
        }

        return true;
    }

    private boolean handleStart(Player player, String[] args) {
        if (!player.hasPermission("csspvp.admin")) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.GENERAL_NO_PERMISSION
            ));
            return true;
        }

        if (args.length < 2) {
            // Try to start the arena the player is in
            Arena playerArena = plugin.getArenaManager().getPlayerArena(player);

            if (playerArena == null) {
                player.sendMessage(plugin.colorize("&cYou need to specify an arena name or be in an arena!"));
                return true;
            }

            playerArena.startArena();
            return true;
        }

        String arenaName = args[1];
        Arena arena = plugin.getArenaManager().getArena(arenaName);

        if (arena == null) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.ARENA_NOT_FOUND,
                    "name", arenaName
            ));
            return true;
        }

        if (!arena.isSetup()) {
            player.sendMessage(plugin.colorize("&cArena is not fully set up yet!"));
            return true;
        }

        arena.startArena();
        player.sendMessage(plugin.colorize("&aStarted arena " + arenaName + "!"));

        return true;
    }

    private boolean handleInfo(Player player, String[] args) {
        if (args.length < 2) {
            // Try to show info for the arena the player is in
            Arena playerArena = plugin.getArenaManager().getPlayerArena(player);

            if (playerArena == null) {
                player.sendMessage(plugin.colorize("&cYou need to specify an arena name or be in an arena!"));
                return true;
            }

            displayArenaInfo(player, playerArena);
            return true;
        }

        String arenaName = args[1];
        Arena arena = plugin.getArenaManager().getArena(arenaName);

        if (arena == null) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.ARENA_NOT_FOUND,
                    "name", arenaName
            ));
            return true;
        }

        displayArenaInfo(player, arena);
        return true;
    }

    private void displayArenaInfo(Player player, Arena arena) {
        player.sendMessage(plugin.colorize("&6=== Arena: " + arena.getName() + " ==="));
        player.sendMessage(plugin.colorize("&eState: &7" + arena.getState().name()));
        player.sendMessage(plugin.colorize("&eSpawn: &7" + (arena.getSpawnPoint() != null ? "Set" : "Not set")));
        player.sendMessage(plugin.colorize("&eCorner 1: &7" + (arena.getCorner1() != null ? "Set" : "Not set")));
        player.sendMessage(plugin.colorize("&eCorner 2: &7" + (arena.getCorner2() != null ? "Set" : "Not set")));
        player.sendMessage(plugin.colorize("&eLives: &7" + arena.getMaxLives()));
        player.sendMessage(plugin.colorize("&eTeam Size: &7" + arena.getTeamSize()));
        player.sendMessage(plugin.colorize("&ePlayers: &7" + arena.getParticipants().size()));
        player.sendMessage(plugin.colorize("&eTeams: &7" + arena.getTeams().size()));
    }

    private boolean handleList(Player player) {
        java.util.Collection<Arena> arenas = plugin.getArenaManager().getAllArenas();

        if (arenas.isEmpty()) {
            player.sendMessage(plugin.colorize("&cNo arenas have been created yet!"));
            return true;
        }

        player.sendMessage(plugin.colorize("&6=== Arenas ==="));

        for (Arena arena : arenas) {
            String status;
            switch (arena.getState()) {
                case ACTIVE:
                    status = "&a[ACTIVE]";
                    break;
                case WAITING:
                    status = "&e[WAITING]";
                    break;
                case ENDING:
                    status = "&c[ENDING]";
                    break;
                default:
                    status = "&7[INACTIVE]";
                    break;
            }

            boolean isSetup = arena.isSetup();
            String setupStatus = isSetup ? "&aReady" : "&cNot ready";

            player.sendMessage(plugin.colorize("&e- " + arena.getName() + " " + status + " &7(" + setupStatus + "&7) &7Players: " + arena.getParticipants().size()));
        }

        return true;
    }
}
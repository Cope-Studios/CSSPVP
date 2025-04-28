package com.copestudios.csspvp.commands;

import com.copestudios.csspvp.CSSPVP;
import com.copestudios.csspvp.messages.MessageManager;
import com.copestudios.csspvp.team.Team;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeamCommands implements CommandExecutor {
    private final CSSPVP plugin;

    public TeamCommands(CSSPVP plugin) {
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
            // Open team menu
            plugin.getGuiManager().openTeamMenu(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
                return handleCreate(player, args);
            case "delete":
                return handleDelete(player, args);
            case "join":
                return handleJoin(player, args);
            case "leave":
                return handleLeave(player);
            case "add":
                return handleAdd(player, args);
            case "remove":
                return handleRemove(player, args);
            case "info":
                return handleInfo(player, args);
            case "list":
                return handleList(player);
            default:
                player.sendMessage(plugin.getMessageManager().getMessage(
                        MessageManager.GENERAL_INVALID_ARGS,
                        "usage", "/cssteam [create|delete|join|leave|add|remove|info|list]"
                ));
                return true;
        }
    }

    private boolean handleCreate(Player player, String[] args) {
        if (!player.hasPermission("csspvp.team")) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.GENERAL_NO_PERMISSION
            ));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.GENERAL_INVALID_ARGS,
                    "usage", "/cssteam create <name>"
            ));
            return true;
        }

        String teamName = args[1];

        if (plugin.getTeamManager().createTeam(teamName)) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.TEAM_CREATED,
                    "name", teamName
            ));

            // Add creator to team
            plugin.getTeamManager().addPlayerToTeam(teamName, player);
        } else {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.TEAM_ALREADY_EXISTS,
                    "name", teamName
            ));
        }

        return true;
    }

    private boolean handleDelete(Player player, String[] args) {
        if (!player.hasPermission("csspvp.admin")) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.GENERAL_NO_PERMISSION
            ));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.GENERAL_INVALID_ARGS,
                    "usage", "/cssteam delete <name>"
            ));
            return true;
        }

        String teamName = args[1];

        if (plugin.getTeamManager().deleteTeam(teamName)) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.TEAM_DELETED,
                    "name", teamName
            ));
        } else {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.TEAM_NOT_FOUND,
                    "name", teamName
            ));
        }

        return true;
    }

    private boolean handleJoin(Player player, String[] args) {
        if (!player.hasPermission("csspvp.team")) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.GENERAL_NO_PERMISSION
            ));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.GENERAL_INVALID_ARGS,
                    "usage", "/cssteam join <name>"
            ));
            return true;
        }

        String teamName = args[1];
        Team team = plugin.getTeamManager().getTeam(teamName);

        if (team == null) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.TEAM_NOT_FOUND,
                    "name", teamName
            ));
            return true;
        }

        // Check if already in the team
        if (team.isMember(player.getUniqueId())) {
            player.sendMessage(plugin.colorize("&cYou are already in this team!"));
            return true;
        }

        // Add to team
        if (plugin.getTeamManager().addPlayerToTeam(teamName, player)) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.TEAM_JOIN,
                    "name", teamName
            ));
        } else {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.TEAM_FULL,
                    "name", teamName
            ));
        }

        return true;
    }

    private boolean handleLeave(Player player) {
        if (!player.hasPermission("csspvp.team")) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.GENERAL_NO_PERMISSION
            ));
            return true;
        }

        // Check if in a team
        Team team = plugin.getTeamManager().getPlayerTeam(player);

        if (team == null) {
            player.sendMessage(plugin.colorize("&cYou are not in any team!"));
            return true;
        }

        // Leave team
        if (plugin.getTeamManager().removePlayerFromTeam(player)) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.TEAM_LEAVE,
                    "name", team.getName()
            ));
        } else {
            player.sendMessage(plugin.colorize("&cFailed to leave team!"));
        }

        return true;
    }

    private boolean handleAdd(Player player, String[] args) {
        if (!player.hasPermission("csspvp.admin")) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.GENERAL_NO_PERMISSION
            ));
            return true;
        }

        if (args.length < 3) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.GENERAL_INVALID_ARGS,
                    "usage", "/cssteam add <name> <player>"
            ));
            return true;
        }

        String teamName = args[1];
        Player target = plugin.getServer().getPlayer(args[2]);

        if (target == null) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.GENERAL_PLAYER_NOT_FOUND,
                    "player", args[2]
            ));
            return true;
        }

        if (plugin.getTeamManager().addPlayerToTeam(teamName, target)) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.TEAM_PLAYER_ADDED,
                    "player", target.getName(),
                    "name", teamName
            ));

            target.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.TEAM_JOIN,
                    "name", teamName
            ));
        } else {
            player.sendMessage(plugin.colorize("&cFailed to add player to team! Team might not exist."));
        }

        return true;
    }

    private boolean handleRemove(Player player, String[] args) {
        if (!player.hasPermission("csspvp.admin")) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.GENERAL_NO_PERMISSION
            ));
            return true;
        }

        if (args.length < 3) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.GENERAL_INVALID_ARGS,
                    "usage", "/cssteam remove <name> <player>"
            ));
            return true;
        }

        String teamName = args[1];
        Team team = plugin.getTeamManager().getTeam(teamName);

        if (team == null) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.TEAM_NOT_FOUND,
                    "name", teamName
            ));
            return true;
        }

        Player target = plugin.getServer().getPlayer(args[2]);

        if (target == null) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.GENERAL_PLAYER_NOT_FOUND,
                    "player", args[2]
            ));
            return true;
        }

        if (team.removeMember(target.getUniqueId())) {
            plugin.getTeamManager().saveTeams();

            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.TEAM_PLAYER_REMOVED,
                    "player", target.getName(),
                    "name", teamName
            ));

            target.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.TEAM_LEAVE,
                    "name", teamName
            ));
        } else {
            player.sendMessage(plugin.colorize("&cPlayer is not in this team!"));
        }

        return true;
    }

    private boolean handleInfo(Player player, String[] args) {
        if (args.length < 2) {
            // Try to show info for the team the player is in
            Team playerTeam = plugin.getTeamManager().getPlayerTeam(player);

            if (playerTeam == null) {
                player.sendMessage(plugin.colorize("&cYou need to specify a team name or be in a team!"));
                return true;
            }

            displayTeamInfo(player, playerTeam);
            return true;
        }

        String teamName = args[1];
        Team team = plugin.getTeamManager().getTeam(teamName);

        if (team == null) {
            player.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.TEAM_NOT_FOUND,
                    "name", teamName
            ));
            return true;
        }

        displayTeamInfo(player, team);
        return true;
    }

    private void displayTeamInfo(Player player, Team team) {
        player.sendMessage(plugin.colorize("&6=== Team: " + team.getName() + " ==="));
        player.sendMessage(plugin.colorize("&eMembers: &7" + team.getSize()));

        if (!team.getMembers().isEmpty()) {
            StringBuilder members = new StringBuilder("&eMembers: &7");
            for (Player member : team.getOnlinePlayers()) {
                members.append(member.getName()).append(", ");
            }

            // Remove trailing comma
            if (members.length() > 11) {
                members.setLength(members.length() - 2);
            }

            player.sendMessage(plugin.colorize(members.toString()));
        }
    }

    private boolean handleList(Player player) {
        java.util.Collection<Team> teams = plugin.getTeamManager().getAllTeams();

        if (teams.isEmpty()) {
            player.sendMessage(plugin.colorize("&cNo teams have been created yet!"));
            return true;
        }

        player.sendMessage(plugin.colorize("&6=== Teams ==="));

        for (Team team : teams) {
            player.sendMessage(plugin.colorize("&e- " + team.getName() + " &7(Members: " + team.getSize() + ")"));
        }

        return true;
    }
}
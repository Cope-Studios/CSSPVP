package com.copestudios.csspvp.team;

import com.copestudios.csspvp.CSSPVP;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TeamManager {
    private final CSSPVP plugin;
    private final Map<String, Team> teams;
    private File teamsFile;

    static {
        // Register Team class for serialization
        ConfigurationSerialization.registerClass(Team.class, "CSSPVP_Team");
    }

    public TeamManager(CSSPVP plugin) {
        this.plugin = plugin;
        this.teams = new HashMap<>();
        this.teamsFile = new File(plugin.getDataFolder(), "teams.yml");
    }

    public void loadTeams() {
        // Create file if it doesn't exist
        if (!teamsFile.exists()) {
            try {
                teamsFile.getParentFile().mkdirs();
                teamsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create teams.yml: " + e.getMessage());
                return;
            }
        }

        // Load teams from file
        FileConfiguration config = YamlConfiguration.loadConfiguration(teamsFile);

        // Clear existing teams
        teams.clear();

        // Load each team
        if (config.contains("teams")) {
            @SuppressWarnings("unchecked")
            List<Map<?, ?>> teamsList = (List<Map<?, ?>>) config.getList("teams");
            if (teamsList != null) {
                for (Map<?, ?> map : teamsList) {
                    try {
                        @SuppressWarnings("unchecked")
                        Team team = Team.deserialize((Map<String, Object>) map);
                        teams.put(team.getName().toLowerCase(), team);
                    } catch (Exception e) {
                        plugin.getLogger().warning("Failed to load team: " + e.getMessage());
                    }
                }
            }
        }

        plugin.getLogger().info("Loaded " + teams.size() + " teams.");
    }

    public void saveTeams() {
        FileConfiguration config = new YamlConfiguration();

        // Convert teams to list for storage
        List<Map<String, Object>> teamsList = new ArrayList<>();
        for (Team team : teams.values()) {
            teamsList.add(team.serialize());
        }

        config.set("teams", teamsList);

        // Save to file
        try {
            config.save(teamsFile);
            plugin.getLogger().info("Saved " + teams.size() + " teams.");
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save teams to file: " + e.getMessage());
        }
    }

    public boolean createTeam(String name) {
        if (teams.containsKey(name.toLowerCase())) {
            return false;
        }

        Team team = new Team(name);
        teams.put(name.toLowerCase(), team);
        saveTeams();
        return true;
    }

    public boolean deleteTeam(String name) {
        if (!teams.containsKey(name.toLowerCase())) {
            return false;
        }

        teams.remove(name.toLowerCase());
        saveTeams();
        return true;
    }

    public Team getTeam(String name) {
        return teams.get(name.toLowerCase());
    }

    public Collection<Team> getAllTeams() {
        return teams.values();
    }

    public Team getPlayerTeam(Player player) {
        return getPlayerTeam(player.getUniqueId());
    }

    public Team getPlayerTeam(UUID playerId) {
        for (Team team : teams.values()) {
            if (team.isMember(playerId)) {
                return team;
            }
        }
        return null;
    }

    public boolean isPlayerInTeam(Player player) {
        return getPlayerTeam(player) != null;
    }

    public boolean addPlayerToTeam(String teamName, Player player) {
        Team team = getTeam(teamName);
        if (team == null) {
            return false;
        }

        // Remove from current team if any
        Team currentTeam = getPlayerTeam(player);
        if (currentTeam != null) {
            currentTeam.removeMember(player.getUniqueId());
        }

        // Add to new team
        team.addMember(player.getUniqueId());
        saveTeams();
        return true;
    }

    public boolean removePlayerFromTeam(Player player) {
        Team team = getPlayerTeam(player);
        if (team == null) {
            return false;
        }

        team.removeMember(player.getUniqueId());
        saveTeams();
        return true;
    }

    public List<Team> getRandomTeams(int count, int playersPerTeam) {
        // Get all online players
        List<Player> allPlayers = new ArrayList<>(plugin.getServer().getOnlinePlayers());
        Collections.shuffle(allPlayers);

        // Create teams
        List<Team> randomTeams = new ArrayList<>();

        for (int i = 0; i < count && !allPlayers.isEmpty(); i++) {
            Team team = new Team("RandomTeam" + (i + 1));

            // Add players to team
            for (int j = 0; j < playersPerTeam && !allPlayers.isEmpty(); j++) {
                Player player = allPlayers.remove(0);
                team.addMember(player.getUniqueId());
            }

            randomTeams.add(team);
        }

        return randomTeams;
    }
}
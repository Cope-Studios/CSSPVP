package com.copestudios.csspvp.arena;

import com.copestudios.csspvp.CSSPVP;
import com.copestudios.csspvp.messages.MessageManager;
import com.copestudios.csspvp.team.Team;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

@SerializableAs("CSSPVP_Arena")
public class Arena implements ConfigurationSerializable {
    private String name;
    private Location spawnPoint;
    private Location corner1;
    private Location corner2;
    private int maxLives;
    private int teamSize;
    private Map<UUID, Integer> participants; // Player UUID to lives remaining
    private List<Team> teams;
    private ArenaState state;

    public enum ArenaState {
        INACTIVE, WAITING, ACTIVE, ENDING
    }

    public Arena(String name) {
        this(name, null, null, null, 3, 1, new HashMap<>(), new ArrayList<>(), ArenaState.INACTIVE);
    }

    public Arena(String name, Location spawnPoint, Location corner1, Location corner2,
                 int maxLives, int teamSize, Map<UUID, Integer> participants,
                 List<Team> teams, ArenaState state) {
        this.name = name;
        this.spawnPoint = spawnPoint;
        this.corner1 = corner1;
        this.corner2 = corner2;
        this.maxLives = maxLives;
        this.teamSize = teamSize;
        this.participants = participants;
        this.teams = teams;
        this.state = state;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        if (spawnPoint != null) map.put("spawnPoint", spawnPoint);
        if (corner1 != null) map.put("corner1", corner1);
        if (corner2 != null) map.put("corner2", corner2);
        map.put("maxLives", maxLives);
        map.put("teamSize", teamSize);

        Map<String, Integer> participantsMap = new HashMap<>();
        for (Map.Entry<UUID, Integer> entry : participants.entrySet()) {
            participantsMap.put(entry.getKey().toString(), entry.getValue());
        }
        map.put("participants", participantsMap);

        List<Map<String, Object>> teamsMap = new ArrayList<>();
        for (Team team : teams) {
            teamsMap.add(team.serialize());
        }
        map.put("teams", teamsMap);

        map.put("state", state.name());

        return map;
    }

    public static Arena deserialize(Map<String, Object> map) {
        String name = (String) map.get("name");
        Location spawnPoint = (Location) map.get("spawnPoint");
        Location corner1 = (Location) map.get("corner1");
        Location corner2 = (Location) map.get("corner2");
        int maxLives = map.containsKey("maxLives") ? (int) map.get("maxLives") : 3;
        int teamSize = map.containsKey("teamSize") ? (int) map.get("teamSize") : 1;

        Map<UUID, Integer> participants = new HashMap<>();
        if (map.containsKey("participants")) {
            @SuppressWarnings("unchecked")
            Map<String, Integer> participantsMap = (Map<String, Integer>) map.get("participants");
            for (Map.Entry<String, Integer> entry : participantsMap.entrySet()) {
                participants.put(UUID.fromString(entry.getKey()), entry.getValue());
            }
        }

        List<Team> teams = new ArrayList<>();
        if (map.containsKey("teams")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> teamsMap = (List<Map<String, Object>>) map.get("teams");
            for (Map<String, Object> teamMap : teamsMap) {
                teams.add(Team.deserialize(teamMap));
            }
        }

        ArenaState state = ArenaState.INACTIVE;
        if (map.containsKey("state")) {
            try {
                state = ArenaState.valueOf((String) map.get("state"));
            } catch (IllegalArgumentException e) {
                // Default to INACTIVE if invalid state
            }
        }

        return new Arena(name, spawnPoint, corner1, corner2, maxLives, teamSize, participants, teams, state);
    }

    public boolean isSetup() {
        return spawnPoint != null && corner1 != null && corner2 != null;
    }

    public boolean isInArena(Location location) {
        if (corner1 == null || corner2 == null) return false;
        if (!corner1.getWorld().getUID().equals(location.getWorld().getUID())) return false;

        double minX = Math.min(corner1.getX(), corner2.getX());
        double minY = Math.min(corner1.getY(), corner2.getY());
        double minZ = Math.min(corner1.getZ(), corner2.getZ());

        double maxX = Math.max(corner1.getX(), corner2.getX());
        double maxY = Math.max(corner1.getY(), corner2.getY());
        double maxZ = Math.max(corner1.getZ(), corner2.getZ());

        return location.getX() >= minX && location.getX() <= maxX &&
                location.getY() >= minY && location.getY() <= maxY &&
                location.getZ() >= minZ && location.getZ() <= maxZ;
    }

    public boolean addPlayer(Player player) {
        // If player is already in an arena, remove them from it first
        Arena currentArena = CSSPVP.getInstance().getArenaManager().getPlayerArena(player);
        if (currentArena != null && !currentArena.equals(this)) {
            currentArena.removePlayer(player);
        }

        if (state == ArenaState.ACTIVE || state == ArenaState.ENDING) {
            return false;
        }

        participants.put(player.getUniqueId(), maxLives);

        // If team-based, try to add to a team
        if (teamSize > 1) {
            // Find a team with space
            Team availableTeam = null;
            for (Team team : teams) {
                if (team.getMembers().size() < teamSize) {
                    availableTeam = team;
                    break;
                }
            }

            if (availableTeam != null) {
                availableTeam.addMember(player.getUniqueId());
            } else {
                // Create a new team if needed
                Team newTeam = new Team("Team" + (teams.size() + 1), new ArrayList<>(Collections.singletonList(player.getUniqueId())));
                teams.add(newTeam);
            }
        }

        // Teleport player to arena spawn point if it exists
        if (spawnPoint != null) {
            player.teleport(spawnPoint);
        }

        // Enable PVP mode for the player
        player.setCanPickupItems(true);

        // Set survival mode
        player.setGameMode(GameMode.SURVIVAL);

        // Send join message
        player.sendMessage(CSSPVP.getInstance().getMessageManager().getMessage(
                MessageManager.ARENA_JOIN,
                "name", name
        ));

        // Check if we should start the arena
        if (state == ArenaState.INACTIVE && !participants.isEmpty()) {
            state = ArenaState.WAITING;
        }

        return true;
    }

    public void removePlayer(Player player) {
        participants.remove(player.getUniqueId());

        // Remove from team if necessary
        if (teamSize > 1) {
            for (Team team : teams) {
                team.removeMember(player.getUniqueId());
            }

            // Clean up empty teams
            teams.removeIf(team -> team.getMembers().isEmpty());
        }

        // Send leave message
        player.sendMessage(CSSPVP.getInstance().getMessageManager().getMessage(
                MessageManager.ARENA_LEAVE,
                "name", name
        ));

        // Check if we need to end the arena
        checkForWinner();
    }

    public void startArena() {
        if (!isSetup()) {
            return;
        }

        state = ArenaState.ACTIVE;

        // Teleport all players to the spawn point
        if (spawnPoint != null) {
            for (UUID uuid : participants.keySet()) {
                Player player = CSSPVP.getInstance().getServer().getPlayer(uuid);
                if (player != null) {
                    player.teleport(spawnPoint);

                    // Make sure players are in survival mode
                    player.setGameMode(GameMode.SURVIVAL);

                    // Reset player's health, food, etc.
                    player.setHealth(player.getMaxHealth());
                    player.setFoodLevel(20);
                    player.setSaturation(20);
                    player.setFireTicks(0);

                    // Enable item pickup
                    player.setCanPickupItems(true);

                    // Clear any potion effects
                    for (PotionEffect effect : player.getActivePotionEffects()) {
                        player.removePotionEffect(effect.getType());
                    }

                    // Announce PVP enabled immediately - no delay
                    player.sendMessage(CSSPVP.getInstance().colorizeString("&a&lPVP is now enabled! Battle begins!"));
                }
            }
        }

        // Broadcast start message
        broadcastMessage(MessageManager.ARENA_START);
    }

    public void endArena() {
        if (state != ArenaState.ACTIVE && state != ArenaState.ENDING) {
            return;
        }

        state = ArenaState.ENDING;

        // Broadcast end message
        broadcastMessage(MessageManager.ARENA_END);

        // Reset arena after a delay
        new BukkitRunnable() {
            @Override
            public void run() {
                resetArena();
            }
        }.runTaskLater(CSSPVP.getInstance(), 100L); // 5 seconds
    }

    public void resetArena() {
        participants.clear();
        teams.clear();
        state = ArenaState.INACTIVE;
    }

    public void onPlayerDeath(Player player, Player killer) {
        Integer lives = participants.get(player.getUniqueId());
        if (lives == null) return;

        // Reduce lives
        if (lives > 1) {
            participants.put(player.getUniqueId(), lives - 1);

            // Inform player of remaining lives
            player.sendMessage(CSSPVP.getInstance().getMessageManager().getMessage(
                    MessageManager.ARENA_DEATH,
                    "lives", String.valueOf(lives - 1)
            ));

            // Respawn at arena spawn
            if (spawnPoint != null) {
                final Location spawn = spawnPoint;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.teleport(spawn);
                    }
                }.runTaskLater(CSSPVP.getInstance(), 1L);
            }
        } else {
            // Player is out of lives
            participants.remove(player.getUniqueId());

            // Set to spectator
            player.sendMessage(CSSPVP.getInstance().getMessageManager().getMessage(
                    MessageManager.ARENA_SPECTATOR,
                    "name", name
            ));

            // Remove from team if needed
            if (teamSize > 1) {
                for (Team team : teams) {
                    team.removeMember(player.getUniqueId());
                }

                // Clean up empty teams
                teams.removeIf(team -> team.getMembers().isEmpty());
            }

            // Teleport to spectator location (above arena)
            if (spawnPoint != null) {
                Location spectatorSpot = spawnPoint.clone().add(0, 5, 0);
                player.teleport(spectatorSpot);
            }

            // Check for winner
            checkForWinner();
        }

        // If there was a killer, send kill message
        if (killer != null) {
            broadcastMessage(MessageManager.ARENA_KILL,
                    "killer", killer.getName(),
                    "victim", player.getName()
            );
        }
    }

    public void checkForWinner() {
        // For team games
        if (teamSize > 1) {
            // Remove empty teams
            teams.removeIf(team -> team.getMembers().isEmpty());

            // Check if only one team remains
            if (teams.size() == 1) {
                Team winningTeam = teams.get(0);
                broadcastMessage(MessageManager.ARENA_WINNER,
                        "player", winningTeam.getName()
                );
                endArena();
                return;
            }
        }
        // For solo games
        else if (participants.size() == 1) {
            // Only one player left, they win
            UUID winnerId = participants.keySet().iterator().next();
            Player winner = CSSPVP.getInstance().getServer().getPlayer(winnerId);
            if (winner != null) {
                broadcastMessage(MessageManager.ARENA_WINNER,
                        "player", winner.getName()
                );
            }
            endArena();
            return;
        }

        // If no players left at all, just end the arena
        if (participants.isEmpty()) {
            endArena();
        }
    }

    private void broadcastMessage(String messageId, Object... placeholders) {
        for (UUID uuid : participants.keySet()) {
            Player player = CSSPVP.getInstance().getServer().getPlayer(uuid);
            if (player != null) {
                player.sendMessage(CSSPVP.getInstance().getMessageManager().getMessage(
                        messageId, placeholders
                ));
            }
        }
    }

    // Getters and setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getSpawnPoint() {
        return spawnPoint;
    }

    public void setSpawnPoint(Location spawnPoint) {
        this.spawnPoint = spawnPoint;
    }

    public Location getCorner1() {
        return corner1;
    }

    public void setCorner1(Location corner1) {
        this.corner1 = corner1;
    }

    public Location getCorner2() {
        return corner2;
    }

    public void setCorner2(Location corner2) {
        this.corner2 = corner2;
    }

    public int getMaxLives() {
        return maxLives;
    }

    public void setMaxLives(int maxLives) {
        this.maxLives = maxLives;
    }

    public int getTeamSize() {
        return teamSize;
    }

    public void setTeamSize(int teamSize) {
        this.teamSize = teamSize;
    }

    public Map<UUID, Integer> getParticipants() {
        return participants;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public ArenaState getState() {
        return state;
    }

    public void setState(ArenaState state) {
        this.state = state;
    }
}
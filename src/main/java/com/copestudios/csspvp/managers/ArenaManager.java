package com.copestudios.csspvp.managers;

import com.copestudios.csspvp.CSSPVP;
import com.copestudios.csspvp.arena.Arena;
import com.copestudios.csspvp.arena.ArenaSettings;
import com.copestudios.csspvp.config.ArenaConfig;
import com.copestudios.csspvp.utils.MessageId;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ArenaManager {

    private final CSSPVP plugin;
    private final ArenaConfig arenaConfig;
    private final Map<String, Arena> arenas;
    private final Map<UUID, String> playerArenas;
    private final Map<UUID, Integer> playerLives;
    private final Map<UUID, Location> previousLocations;

    public ArenaManager(CSSPVP plugin) {
        this.plugin = plugin;
        this.arenaConfig = new ArenaConfig(plugin);
        this.arenas = new HashMap<>();
        this.playerArenas = new HashMap<>();
        this.playerLives = new HashMap<>();
        this.previousLocations = new HashMap<>();

        loadArenas();
    }

    private void loadArenas() {
        List<Arena> loadedArenas = arenaConfig.loadArenas();
        for (Arena arena : loadedArenas) {
            arenas.put(arena.getName().toLowerCase(), arena);
        }
    }

    public void saveArenas() {
        for (Arena arena : arenas.values()) {
            arenaConfig.saveArena(arena);
        }
    }

    public boolean createArena(String name, List<Location> corners, Location spawnLocation) {
        String lowerName = name.toLowerCase();
        if (arenas.containsKey(lowerName)) {
            return false;
        }

        ArenaSettings defaultSettings = new ArenaSettings(3, false, 30, 15, 10, 3);
        Arena arena = new Arena(name, corners, spawnLocation, defaultSettings);
        arenas.put(lowerName, arena);
        arenaConfig.saveArena(arena);

        return true;
    }

    public boolean deleteArena(String name) {
        String lowerName = name.toLowerCase();
        if (!arenas.containsKey(lowerName)) {
            return false;
        }

        // Remove all players from the arena
        Arena arena = arenas.get(lowerName);
        for (UUID playerId : new ArrayList<>(arena.getPlayers())) {
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null) {
                leaveArena(player);
            } else {
                arena.removePlayer(playerId);
                playerArenas.remove(playerId);
                playerLives.remove(playerId);
            }
        }

        arenas.remove(lowerName);
        return true;
    }

    public Arena getArena(String name) {
        return arenas.get(name.toLowerCase());
    }

    public Set<String> getArenaNames() {
        return new HashSet<>(arenas.keySet());
    }

    public Arena getPlayerArena(Player player) {
        String arenaName = playerArenas.get(player.getUniqueId());
        if (arenaName == null) {
            return null;
        }
        return arenas.get(arenaName.toLowerCase());
    }

    public boolean isInArena(Player player) {
        return playerArenas.containsKey(player.getUniqueId());
    }

    public boolean joinArena(Player player, String arenaName) {
        String lowerName = arenaName.toLowerCase();
        if (!arenas.containsKey(lowerName)) {
            player.sendMessage(MessageId.get("arena.not-found", "name", arenaName));
            return false;
        }

        if (isInArena(player)) {
            player.sendMessage(MessageId.get("arena.already-in"));
            return false;
        }

        Arena arena = arenas.get(lowerName);
        if (arena.isFull()) {
            player.sendMessage(MessageId.get("arena.full", "name", arenaName));
            return false;
        }

        // Store previous location
        previousLocations.put(player.getUniqueId(), player.getLocation());

        // Add player to arena
        arena.addPlayer(player.getUniqueId());
        playerArenas.put(player.getUniqueId(), lowerName);

        // Set initial lives
        if (!arena.getSettings().hasInfiniteLives()) {
            playerLives.put(player.getUniqueId(), arena.getSettings().getLives());
        }

        // Teleport to arena spawn
        player.teleport(arena.getSpawnLocation());

        // Add player to cache for random duels
        arenaConfig.addPlayerToCache(lowerName, player.getName());

        player.sendMessage(MessageId.get("arena.join", "name", arenaName));
        return true;
    }

    public boolean leaveArena(Player player) {
        if (!isInArena(player)) {
            player.sendMessage(MessageId.get("arena.not-in"));
            return false;
        }

        String arenaName = playerArenas.get(player.getUniqueId());
        Arena arena = arenas.get(arenaName.toLowerCase());

        // Remove player from arena
        arena.removePlayer(player.getUniqueId());
        playerArenas.remove(player.getUniqueId());
        playerLives.remove(player.getUniqueId());

        // Remove from cache
        arenaConfig.removePlayerFromCache(arenaName.toLowerCase(), player.getName());

        // Return to previous location or lobby
        Location location = previousLocations.remove(player.getUniqueId());
        if (location != null) {
            player.teleport(location);
        } else {
            // Teleport to lobby if exists
            Location lobby = plugin.getConfigManager().getLobbyLocation();
            if (lobby != null) {
                player.teleport(lobby);
            }
        }

        // Clear inventory
        player.getInventory().clear();

        // Reset game mode
        player.setGameMode(GameMode.SURVIVAL);

        player.sendMessage(MessageId.get("arena.leave", "name", arenaName));
        return true;
    }

    public void reducePlayerLife(Player player) {
        if (!isInArena(player)) {
            return;
        }

        Arena arena = getPlayerArena(player);
        if (arena == null || arena.getSettings().hasInfiniteLives()) {
            return;
        }

        int lives = playerLives.getOrDefault(player.getUniqueId(), 1);
        lives--;

        if (lives <= 0) {
            // Last life lost, set to spectator
            player.setGameMode(GameMode.SPECTATOR);
            playerLives.remove(player.getUniqueId());
        } else {
            playerLives.put(player.getUniqueId(), lives);
        }
    }

    public int getPlayerLives(Player player) {
        if (!isInArena(player)) {
            return 0;
        }

        Arena arena = getPlayerArena(player);
        if (arena == null) {
            return 0;
        }

        if (arena.getSettings().hasInfiniteLives()) {
            return -1; // Infinite
        }

        return playerLives.getOrDefault(player.getUniqueId(), 0);
    }

    public void setKit(String arenaName, ItemStack[] contents) {
        arenaConfig.saveKit(arenaName.toLowerCase(), contents);
    }

    public ItemStack[] getKit(String arenaName) {
        return arenaConfig.loadKit(arenaName.toLowerCase());
    }

    public void cleanUp() {
        playerArenas.clear();
        playerLives.clear();
        previousLocations.clear();

        for (Arena arena : arenas.values()) {
            arena.clearPlayers();
        }
    }
}
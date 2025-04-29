package com.copestudios.csspvp.arena;

import com.copestudios.csspvp.CSSPVP;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.Location;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ArenaManager {
    private final CSSPVP plugin;
    private final Map<String, Arena> arenas;
    private File arenasFile;

    // Store players that joined arenas for rduel command
    private final Map<String, List<UUID>> arenaPlayersHistory = new HashMap<>();

    // Store previous locations for teleportation after duel
    private final Map<UUID, Location> playerPreviousLocations = new HashMap<>();

    static {
        // Register Arena class for serialization
        ConfigurationSerialization.registerClass(Arena.class, "CSSPVP_Arena");
    }

    public ArenaManager(CSSPVP plugin) {
        this.plugin = plugin;
        this.arenas = new HashMap<>();
        this.arenasFile = new File(plugin.getDataFolder(), "arenas.yml");
    }

    /**
     * Store a player's previous location before teleporting to an arena
     */
    public void storePlayerLocation(Player player) {
        playerPreviousLocations.put(player.getUniqueId(), player.getLocation());
    }

    /**
     * Get a player's previous location before they entered an arena
     */
    public Location getPlayerPreviousLocation(Player player) {
        return playerPreviousLocations.get(player.getUniqueId());
    }

    /**
     * Remove a player's previous location from storage
     */
    public void removePlayerPreviousLocation(Player player) {
        playerPreviousLocations.remove(player.getUniqueId());
    }

    /**
     * Record that a player joined an arena for rduel command
     */
    public void recordPlayerJoinedArena(String arenaName, Player player) {
        List<UUID> players = arenaPlayersHistory.computeIfAbsent(arenaName, k -> new ArrayList<>());

        // Don't add duplicates
        if (!players.contains(player.getUniqueId())) {
            players.add(player.getUniqueId());
        }

        // Keep only the last 20 players to avoid memory leaks
        if (players.size() > 20) {
            players.remove(0);
        }
    }

    /**
     * Get random players from an arena's history
     */
    public List<Player> getRandomPlayersFromArenaHistory(String arenaName, int count) {
        List<UUID> players = arenaPlayersHistory.get(arenaName);
        if (players == null || players.isEmpty()) {
            return new ArrayList<>();
        }

        // Shuffle the list
        List<UUID> shuffled = new ArrayList<>(players);
        Collections.shuffle(shuffled);

        // Get the requested number of players
        List<Player> result = new ArrayList<>();
        for (UUID uuid : shuffled) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null && player.isOnline()) {
                result.add(player);
                if (result.size() >= count) {
                    break;
                }
            }
        }

        return result;
    }

    public void loadArenas() {
        // Create file if it doesn't exist
        if (!arenasFile.exists()) {
            try {
                arenasFile.getParentFile().mkdirs();
                arenasFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create arenas.yml: " + e.getMessage());
                return;
            }
        }

        // Load arenas from file
        FileConfiguration config = YamlConfiguration.loadConfiguration(arenasFile);

        // Clear existing arenas
        arenas.clear();

        // Load each arena
        if (config.contains("arenas")) {
            @SuppressWarnings("unchecked")
            List<Map<?, ?>> arenasList = (List<Map<?, ?>>) config.getList("arenas");
            if (arenasList != null) {
                for (Map<?, ?> map : arenasList) {
                    try {
                        @SuppressWarnings("unchecked")
                        Arena arena = Arena.deserialize((Map<String, Object>) map);
                        arenas.put(arena.getName().toLowerCase(), arena);
                    } catch (Exception e) {
                        plugin.getLogger().warning("Failed to load arena: " + e.getMessage());
                    }
                }
            }
        }

        plugin.getLogger().info("Loaded " + arenas.size() + " arenas.");
    }

    public void saveArenas() {
        FileConfiguration config = new YamlConfiguration();

        // Convert arenas to list for storage
        List<Map<String, Object>> arenasList = new ArrayList<>();
        for (Arena arena : arenas.values()) {
            arenasList.add(arena.serialize());
        }

        config.set("arenas", arenasList);

        // Save to file
        try {
            config.save(arenasFile);
            plugin.getLogger().info("Saved " + arenas.size() + " arenas.");
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save arenas to file: " + e.getMessage());
        }
    }

    public boolean createArena(String name) {
        if (arenas.containsKey(name.toLowerCase())) {
            return false;
        }

        Arena arena = new Arena(name);
        arenas.put(name.toLowerCase(), arena);
        saveArenas();
        return true;
    }

    public boolean deleteArena(String name) {
        if (!arenas.containsKey(name.toLowerCase())) {
            return false;
        }

        arenas.remove(name.toLowerCase());
        saveArenas();
        return true;
    }

    public Arena getArena(String name) {
        return arenas.get(name.toLowerCase());
    }

    public Collection<Arena> getAllArenas() {
        return arenas.values();
    }

    public Arena getPlayerArena(Player player) {
        for (Arena arena : arenas.values()) {
            if (arena.getParticipants().containsKey(player.getUniqueId())) {
                return arena;
            }
        }
        return null;
    }

    public boolean isPlayerInArena(Player player) {
        return getPlayerArena(player) != null;
    }

    public boolean isLocationInAnyArena(Location location) {
        for (Arena arena : arenas.values()) {
            if (arena.isInArena(location)) {
                return true;
            }
        }
        return false;
    }

    public Arena getArenaAtLocation(Location location) {
        for (Arena arena : arenas.values()) {
            if (arena.isInArena(location)) {
                return arena;
            }
        }
        return null;
    }

    public Arena getRandomActiveArena() {
        List<Arena> activeArenas = new ArrayList<>();

        for (Arena arena : arenas.values()) {
            if (arena.isSetup() && arena.getState() == Arena.ArenaState.INACTIVE) {
                activeArenas.add(arena);
            }
        }

        if (activeArenas.isEmpty()) {
            return null;
        }

        return activeArenas.get(new Random().nextInt(activeArenas.size()));
    }
}
package com.copestudios.csspvp.config;

import com.copestudios.csspvp.CSSPVP;
import com.copestudios.csspvp.arena.Arena;
import com.copestudios.csspvp.arena.ArenaSettings;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class ArenaConfig {

    private final CSSPVP plugin;

    public ArenaConfig(CSSPVP plugin) {
        this.plugin = plugin;
    }

    public void saveArena(Arena arena) {
        FileConfiguration arenas = plugin.getConfigManager().getArenas();

        String path = "arenas." + arena.getName() + ".";

        // Save arena corners
        for (int i = 0; i < arena.getCorners().size(); i++) {
            Location corner = arena.getCorners().get(i);
            arenas.set(path + "corners." + i + ".world", corner.getWorld().getName());
            arenas.set(path + "corners." + i + ".x", corner.getX());
            arenas.set(path + "corners." + i + ".y", corner.getY());
            arenas.set(path + "corners." + i + ".z", corner.getZ());
        }

        // Save spawn location
        Location spawn = arena.getSpawnLocation();
        arenas.set(path + "spawn.world", spawn.getWorld().getName());
        arenas.set(path + "spawn.x", spawn.getX());
        arenas.set(path + "spawn.y", spawn.getY());
        arenas.set(path + "spawn.z", spawn.getZ());
        arenas.set(path + "spawn.yaw", spawn.getYaw());
        arenas.set(path + "spawn.pitch", spawn.getPitch());

        // Save settings
        ArenaSettings settings = arena.getSettings();
        arenas.set(path + "settings.lives", settings.getLives());
        arenas.set(path + "settings.drop-items", settings.canDropItems());
        arenas.set(path + "settings.notch-apple-delay", settings.getNotchAppleDelay());
        arenas.set(path + "settings.enderpearl-delay", settings.getEnderpearlDelay());
        arenas.set(path + "settings.max-players", settings.getMaxPlayers());
        arenas.set(path + "settings.kit-delay", settings.getKitDelay());

        plugin.getConfigManager().saveArenas();

        // Create cache file for this arena
        createCacheFile(arena.getName());
    }

    public List<Arena> loadArenas() {
        List<Arena> arenaList = new ArrayList<>();
        FileConfiguration arenas = plugin.getConfigManager().getArenas();

        ConfigurationSection arenasSection = arenas.getConfigurationSection("arenas");
        if (arenasSection == null) {
            return arenaList;
        }

        for (String arenaName : arenasSection.getKeys(false)) {
            String path = "arenas." + arenaName + ".";

            // Load corners
            List<Location> corners = new ArrayList<>();
            ConfigurationSection cornersSection = arenas.getConfigurationSection(path + "corners");
            if (cornersSection != null) {
                for (String key : cornersSection.getKeys(false)) {
                    String cornerPath = path + "corners." + key + ".";
                    World world = Bukkit.getWorld(arenas.getString(cornerPath + "world"));
                    double x = arenas.getDouble(cornerPath + "x");
                    double y = arenas.getDouble(cornerPath + "y");
                    double z = arenas.getDouble(cornerPath + "z");
                    corners.add(new Location(world, x, y, z));
                }
            }

            // Load spawn location
            World world = Bukkit.getWorld(arenas.getString(path + "spawn.world"));
            double x = arenas.getDouble(path + "spawn.x");
            double y = arenas.getDouble(path + "spawn.y");
            double z = arenas.getDouble(path + "spawn.z");
            float yaw = (float) arenas.getDouble(path + "spawn.yaw");
            float pitch = (float) arenas.getDouble(path + "spawn.pitch");
            Location spawn = new Location(world, x, y, z, yaw, pitch);

            // Load settings
            int lives = arenas.getInt(path + "settings.lives", 3);
            boolean dropItems = arenas.getBoolean(path + "settings.drop-items", false);
            int notchAppleDelay = arenas.getInt(path + "settings.notch-apple-delay", 30);
            int enderpearlDelay = arenas.getInt(path + "settings.enderpearl-delay", 15);
            int maxPlayers = arenas.getInt(path + "settings.max-players", 10);
            int kitDelay = arenas.getInt(path + "settings.kit-delay", 3);

            // Create settings
            ArenaSettings settings = new ArenaSettings(lives, dropItems, notchAppleDelay, enderpearlDelay, maxPlayers, kitDelay);

            // Create and add arena
            Arena arena = new Arena(arenaName, corners, spawn, settings);
            arenaList.add(arena);
        }

        return arenaList;
    }

    public void saveKit(String arenaName, ItemStack[] contents) {
        FileConfiguration arenas = plugin.getConfigManager().getArenas();
        arenas.set("arenas." + arenaName + ".kit", contents);
        plugin.getConfigManager().saveArenas();
    }

    public ItemStack[] loadKit(String arenaName) {
        FileConfiguration arenas = plugin.getConfigManager().getArenas();
        return (ItemStack[]) arenas.get("arenas." + arenaName + ".kit");
    }

    private void createCacheFile(String arenaName) {
        File cacheFile = new File(plugin.getDataFolder() + "/cache", arenaName + "-cache.yml");
        if (!cacheFile.exists()) {
            try {
                cacheFile.createNewFile();
                YamlConfiguration config = YamlConfiguration.loadConfiguration(cacheFile);
                config.set("players", new ArrayList<String>());
                config.save(cacheFile);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create cache file for arena " + arenaName, e);
            }
        }
    }

    public void addPlayerToCache(String arenaName, String playerName) {
        File cacheFile = new File(plugin.getDataFolder() + "/cache", arenaName + "-cache.yml");
        if (cacheFile.exists()) {
            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(cacheFile);
                List<String> players = config.getStringList("players");
                if (!players.contains(playerName)) {
                    players.add(playerName);
                    config.set("players", players);
                    config.save(cacheFile);
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not update cache file for arena " + arenaName, e);
            }
        }
    }

    public void removePlayerFromCache(String arenaName, String playerName) {
        File cacheFile = new File(plugin.getDataFolder() + "/cache", arenaName + "-cache.yml");
        if (cacheFile.exists()) {
            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(cacheFile);
                List<String> players = config.getStringList("players");
                if (players.contains(playerName)) {
                    players.remove(playerName);
                    config.set("players", players);
                    config.save(cacheFile);
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not update cache file for arena " + arenaName, e);
            }
        }
    }

    public List<String> getPlayersInCache(String arenaName) {
        File cacheFile = new File(plugin.getDataFolder() + "/cache", arenaName + "-cache.yml");
        if (cacheFile.exists()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(cacheFile);
            return config.getStringList("players");
        }
        return new ArrayList<>();
    }
}
package com.copestudios.csspvp.config;

import com.copestudios.csspvp.CSSPVP;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class ConfigManager {

    private final CSSPVP plugin;

    private FileConfiguration config;
    private File configFile;

    private FileConfiguration arenas;
    private File arenasFile;

    private FileConfiguration zones;
    private File zonesFile;

    private FileConfiguration messages;
    private File messagesFile;

    private FileConfiguration duelData;
    private File duelDataFile;

    public ConfigManager(CSSPVP plugin) {
        this.plugin = plugin;
    }

    public void setupConfigs() {
        // Main config.yml
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }

        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        setupDefaultConfig();

        // Arenas config
        arenasFile = new File(plugin.getDataFolder(), "arenas.yml");
        if (!arenasFile.exists()) {
            try {
                arenasFile.createNewFile();
                arenas = YamlConfiguration.loadConfiguration(arenasFile);
                arenas.save(arenasFile);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create arenas.yml", e);
            }
        } else {
            arenas = YamlConfiguration.loadConfiguration(arenasFile);
        }

        // Zones config
        zonesFile = new File(plugin.getDataFolder(), "zones.yml");
        if (!zonesFile.exists()) {
            try {
                zonesFile.createNewFile();
                zones = YamlConfiguration.loadConfiguration(zonesFile);
                zones.save(zonesFile);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create zones.yml", e);
            }
        } else {
            zones = YamlConfiguration.loadConfiguration(zonesFile);
        }

        // Messages config
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);

        // Duel data config
        duelDataFile = new File(plugin.getDataFolder(), "dueldata.yml");
        if (!duelDataFile.exists()) {
            try {
                duelDataFile.createNewFile();
                duelData = YamlConfiguration.loadConfiguration(duelDataFile);
                duelData.save(duelDataFile);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create dueldata.yml", e);
            }
        } else {
            duelData = YamlConfiguration.loadConfiguration(duelDataFile);
        }

        // Create cache directories for arenas
        File cacheDir = new File(plugin.getDataFolder(), "cache");
        if (!cacheDir.exists()) {
            cacheDir.mkdir();
        }
    }
    private void setupDefaultConfig() {
        // Set default values if they don't exist
        config.addDefault("lobby.prevent-block-break", true);
        config.addDefault("lobby.prevent-block-place", true);
        config.addDefault("lobby.prevent-damage", true);
        config.addDefault("lobby.prevent-player-damage", true);
        config.addDefault("lobby.op-bypass", true);
        config.addDefault("arena.default-kit", "default");
        config.addDefault("duel.use-arena-inventory", true);
        config.addDefault("duel.request-timeout", 60);
        config.addDefault("duel.countdown", 10);
        config.addDefault("random-duel.countdown", 5);
        config.options().copyDefaults(true);
        saveConfig();
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getArenas() {
        return arenas;
    }

    public FileConfiguration getZones() {
        return zones;
    }

    public FileConfiguration getMessages() {
        return messages;
    }

    public FileConfiguration getDuelData() {
        return duelData;
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config.yml", e);
        }
    }

    public void saveArenas() {
        try {
            arenas.save(arenasFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save arenas.yml", e);
        }
    }

    public void saveZones() {
        try {
            zones.save(zonesFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save zones.yml", e);
        }
    }

    public void saveMessages() {
        try {
            messages.save(messagesFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save messages.yml", e);
        }
    }

    public void saveDuelData() {
        try {
            duelData.save(duelDataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save dueldata.yml", e);
        }
    }

    public void saveAllConfigs() {
        saveConfig();
        saveArenas();
        saveZones();
        saveMessages();
        saveDuelData();
    }

    public void reloadAllConfigs() {
        config = YamlConfiguration.loadConfiguration(configFile);
        arenas = YamlConfiguration.loadConfiguration(arenasFile);
        zones = YamlConfiguration.loadConfiguration(zonesFile);
        messages = YamlConfiguration.loadConfiguration(messagesFile);
        duelData = YamlConfiguration.loadConfiguration(duelDataFile);
    }

    public Location getLobbyLocation() {
        if (!config.contains("lobby.world")) {
            return null;
        }

        World world = Bukkit.getWorld(config.getString("lobby.world"));
        double x = config.getDouble("lobby.x");
        double y = config.getDouble("lobby.y");
        double z = config.getDouble("lobby.z");
        float yaw = (float) config.getDouble("lobby.yaw");
        float pitch = (float) config.getDouble("lobby.pitch");

        return new Location(world, x, y, z, yaw, pitch);
    }

    public void setLobbyLocation(Location location) {
        config.set("lobby.world", location.getWorld().getName());
        config.set("lobby.x", location.getX());
        config.set("lobby.y", location.getY());
        config.set("lobby.z", location.getZ());
        config.set("lobby.yaw", location.getYaw());
        config.set("lobby.pitch", location.getPitch());
        saveConfig();
    }
}
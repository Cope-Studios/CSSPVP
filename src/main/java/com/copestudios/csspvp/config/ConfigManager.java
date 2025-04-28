package com.copestudios.csspvp.config;

import com.copestudios.csspvp.CSSPVP;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private final CSSPVP plugin;
    private FileConfiguration config;
    private File configFile;

    public ConfigManager(CSSPVP plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        // Create default config if it doesn't exist
        if (!new File(plugin.getDataFolder(), "config.yml").exists()) {
            plugin.saveDefaultConfig();
        }

        // Load config
        configFile = new File(plugin.getDataFolder(), "config.yml");
        config = YamlConfiguration.loadConfiguration(configFile);

        // Set defaults if they don't exist
        setDefaults();

        plugin.getLogger().info("Configuration loaded successfully!");
    }

    private void setDefaults() {
        Map<String, Object> defaults = new HashMap<>();
        defaults.put("general.spawn-enabled", true);
        defaults.put("general.spawn-location", null);
        defaults.put("arena.default-lives", 3);
        defaults.put("arena.default-team-size", 1);
        defaults.put("arena.enable-random-duels", true);
        defaults.put("gui.title-color", "#FF5555");
        defaults.put("gui.button-color", "#55FF55");
        defaults.put("gui.border-color", "#5555FF");
        defaults.put("protection.prevent-pvp-outside-arena", true);
        defaults.put("protection.prevent-block-break", true);
        defaults.put("protection.prevent-block-place", true);
        defaults.put("protection.prevent-item-drop", true);
        defaults.put("protection.prevent-hunger", true);

        boolean needsSave = false;

        for (Map.Entry<String, Object> entry : defaults.entrySet()) {
            String path = entry.getKey();
            Object defaultValue = entry.getValue();

            if (!config.contains(path)) {
                config.set(path, defaultValue);
                needsSave = true;
            }
        }

        if (needsSave) {
            saveConfig();
        }
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (Exception ex) {
            plugin.getLogger().severe("Could not save config to " + configFile.getName() + ": " + ex.getMessage());
        }
    }

    public String getString(String path, String defaultValue) {
        return config.getString(path, defaultValue);
    }

    public int getInt(String path, int defaultValue) {
        return config.getInt(path, defaultValue);
    }

    public boolean getBoolean(String path, boolean defaultValue) {
        return config.getBoolean(path, defaultValue);
    }

    public Location getLocation(String path) {
        return config.getLocation(path);
    }

    public void setLocation(String path, Location location) {
        config.set(path, location);
        saveConfig();
    }

    public void set(String path, Object value) {
        config.set(path, value);
        saveConfig();
    }

    public Location getGeneralSpawn() {
        return getLocation("general.spawn-location");
    }

    public void setGeneralSpawn(Location location) {
        setLocation("general.spawn-location", location);
    }

    public boolean isPvpOutsideArenaAllowed() {
        return !getBoolean("protection.prevent-pvp-outside-arena", true);
    }

    public boolean isBlockBreakAllowed() {
        return !getBoolean("protection.prevent-block-break", true);
    }

    public boolean isBlockPlaceAllowed() {
        return !getBoolean("protection.prevent-block-place", true);
    }

    public boolean isItemDropAllowed() {
        return !getBoolean("protection.prevent-item-drop", true);
    }

    public boolean isHungerEnabled() {
        return !getBoolean("protection.prevent-hunger", true);
    }

    public int getDefaultLives() {
        return getInt("arena.default-lives", 3);
    }

    public int getDefaultTeamSize() {
        return getInt("arena.default-team-size", 1);
    }

    public boolean isRandomDuelsEnabled() {
        return getBoolean("arena.enable-random-duels", true);
    }

    public String getTitleColor() {
        return getString("gui.title-color", "#FF5555");
    }

    public String getButtonColor() {
        return getString("gui.button-color", "#55FF55");
    }

    public String getBorderColor() {
        return getString("gui.border-color", "#5555FF");
    }
}
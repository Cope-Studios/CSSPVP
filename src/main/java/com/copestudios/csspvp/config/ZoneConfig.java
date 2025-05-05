package com.copestudios.csspvp.config;

import com.copestudios.csspvp.CSSPVP;
import com.copestudios.csspvp.duel.DuelZone;
import com.copestudios.csspvp.duel.ZoneSettings;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ZoneConfig {

    private final CSSPVP plugin;

    public ZoneConfig(CSSPVP plugin) {
        this.plugin = plugin;
    }

    public void saveZone(DuelZone zone) {
        FileConfiguration zones = plugin.getConfigManager().getZones();

        String path = "zones." + zone.getName() + ".";

        // Save zone corners
        for (int i = 0; i < zone.getCorners().size(); i++) {
            Location corner = zone.getCorners().get(i);
            zones.set(path + "corners." + i + ".world", corner.getWorld().getName());
            zones.set(path + "corners." + i + ".x", corner.getX());
            zones.set(path + "corners." + i + ".y", corner.getY());
            zones.set(path + "corners." + i + ".z", corner.getZ());
        }

        // Save spawn locations
        Location spawn1 = zone.getSpawnLocation1();
        zones.set(path + "spawn1.world", spawn1.getWorld().getName());
        zones.set(path + "spawn1.x", spawn1.getX());
        zones.set(path + "spawn1.y", spawn1.getY());
        zones.set(path + "spawn1.z", spawn1.getZ());
        zones.set(path + "spawn1.yaw", spawn1.getYaw());
        zones.set(path + "spawn1.pitch", spawn1.getPitch());

        Location spawn2 = zone.getSpawnLocation2();
        zones.set(path + "spawn2.world", spawn2.getWorld().getName());
        zones.set(path + "spawn2.x", spawn2.getX());
        zones.set(path + "spawn2.y", spawn2.getY());
        zones.set(path + "spawn2.z", spawn2.getZ());
        zones.set(path + "spawn2.yaw", spawn2.getYaw());
        zones.set(path + "spawn2.pitch", spawn2.getPitch());

        // Save settings
        ZoneSettings settings = zone.getSettings();
        zones.set(path + "settings.drop-items", settings.canDropItems());
        zones.set(path + "settings.notch-apple-delay", settings.getNotchAppleDelay());
        zones.set(path + "settings.enderpearl-delay", settings.getEnderpearlDelay());

        plugin.getConfigManager().saveZones();
    }

    public List<DuelZone> loadZones() {
        List<DuelZone> zoneList = new ArrayList<>();
        FileConfiguration zones = plugin.getConfigManager().getZones();

        ConfigurationSection zonesSection = zones.getConfigurationSection("zones");
        if (zonesSection == null) {
            return zoneList;
        }

        for (String zoneName : zonesSection.getKeys(false)) {
            String path = "zones." + zoneName + ".";

            // Load corners
            List<Location> corners = new ArrayList<>();
            ConfigurationSection cornersSection = zones.getConfigurationSection(path + "corners");
            if (cornersSection != null) {
                for (String key : cornersSection.getKeys(false)) {
                    String cornerPath = path + "corners." + key + ".";
                    World world = Bukkit.getWorld(zones.getString(cornerPath + "world"));
                    double x = zones.getDouble(cornerPath + "x");
                    double y = zones.getDouble(cornerPath + "y");
                    double z = zones.getDouble(cornerPath + "z");
                    corners.add(new Location(world, x, y, z));
                }
            }

            // Load spawn locations
            World world1 = Bukkit.getWorld(zones.getString(path + "spawn1.world"));
            double x1 = zones.getDouble(path + "spawn1.x");
            double y1 = zones.getDouble(path + "spawn1.y");
            double z1 = zones.getDouble(path + "spawn1.z");
            float yaw1 = (float) zones.getDouble(path + "spawn1.yaw");
            float pitch1 = (float) zones.getDouble(path + "spawn1.pitch");
            Location spawn1 = new Location(world1, x1, y1, z1, yaw1, pitch1);

            World world2 = Bukkit.getWorld(zones.getString(path + "spawn2.world"));
            double x2 = zones.getDouble(path + "spawn2.x");
            double y2 = zones.getDouble(path + "spawn2.y");
            double z2 = zones.getDouble(path + "spawn2.z");
            float yaw2 = (float) zones.getDouble(path + "spawn2.yaw");
            float pitch2 = (float) zones.getDouble(path + "spawn2.pitch");
            Location spawn2 = new Location(world2, x2, y2, z2, yaw2, pitch2);

            // Load settings
            boolean dropItems = zones.getBoolean(path + "settings.drop-items", false);
            int notchAppleDelay = zones.getInt(path + "settings.notch-apple-delay", 30);
            int enderpearlDelay = zones.getInt(path + "settings.enderpearl-delay", 15);

            // Create settings
            ZoneSettings settings = new ZoneSettings(dropItems, notchAppleDelay, enderpearlDelay);

            // Create and add zone
            DuelZone zone = new DuelZone(zoneName, corners, spawn1, spawn2, settings);
            zoneList.add(zone);
        }

        return zoneList;
    }

    public void saveKit(String zoneName, ItemStack[] contents) {
        FileConfiguration zones = plugin.getConfigManager().getZones();
        zones.set("zones." + zoneName + ".kit", contents);
        plugin.getConfigManager().saveZones();
    }

    public ItemStack[] loadKit(String zoneName) {
        FileConfiguration zones = plugin.getConfigManager().getZones();
        return (ItemStack[]) zones.get("zones." + zoneName + ".kit");
    }
}
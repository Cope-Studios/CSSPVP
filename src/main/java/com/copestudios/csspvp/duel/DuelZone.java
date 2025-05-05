package com.copestudios.csspvp.duel;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class DuelZone {

    private final String name;
    private final List<Location> corners;
    private final Location spawnLocation1;
    private final Location spawnLocation2;
    private final ZoneSettings settings;
    private boolean inUse;

    public DuelZone(String name, List<Location> corners, Location spawnLocation1, Location spawnLocation2, ZoneSettings settings) {
        this.name = name;
        this.corners = corners;
        this.spawnLocation1 = spawnLocation1;
        this.spawnLocation2 = spawnLocation2;
        this.settings = settings;
        this.inUse = false;
    }

    public String getName() {
        return name;
    }

    public List<Location> getCorners() {
        return new ArrayList<>(corners);
    }

    public Location getSpawnLocation1() {
        return spawnLocation1.clone();
    }

    public Location getSpawnLocation2() {
        return spawnLocation2.clone();
    }

    public ZoneSettings getSettings() {
        return settings;
    }

    public boolean isInUse() {
        return inUse;
    }

    public void setInUse(boolean inUse) {
        this.inUse = inUse;
    }

    public boolean isInside(Location location) {
        if (corners.size() < 2) {
            return false;
        }

        // Simple 2D box check (ignoring Y for simplicity)
        double minX = Double.MAX_VALUE;
        double minZ = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxZ = Double.MIN_VALUE;

        for (Location corner : corners) {
            minX = Math.min(minX, corner.getX());
            minZ = Math.min(minZ, corner.getZ());
            maxX = Math.max(maxX, corner.getX());
            maxZ = Math.max(maxZ, corner.getZ());
        }

        return location.getX() >= minX && location.getX() <= maxX
                && location.getZ() >= minZ && location.getZ() <= maxZ;
    }
}
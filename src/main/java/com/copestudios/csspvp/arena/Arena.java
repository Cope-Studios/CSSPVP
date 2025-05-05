package com.copestudios.csspvp.arena;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Arena {

    private final String name;
    private final List<Location> corners;
    private final Location spawnLocation;
    private final ArenaSettings settings;
    private final Set<UUID> players;

    public Arena(String name, List<Location> corners, Location spawnLocation, ArenaSettings settings) {
        this.name = name;
        this.corners = corners;
        this.spawnLocation = spawnLocation;
        this.settings = settings;
        this.players = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public List<Location> getCorners() {
        return new ArrayList<>(corners);
    }

    public Location getSpawnLocation() {
        return spawnLocation.clone();
    }

    public ArenaSettings getSettings() {
        return settings;
    }

    public Set<UUID> getPlayers() {
        return new HashSet<>(players);
    }

    public int getPlayerCount() {
        return players.size();
    }

    public boolean isFull() {
        return settings.getMaxPlayers() != -1 && players.size() >= settings.getMaxPlayers();
    }

    public boolean isInArena(UUID player) {
        return players.contains(player);
    }

    public boolean addPlayer(UUID player) {
        if (isFull()) {
            return false;
        }
        return players.add(player);
    }

    public boolean removePlayer(UUID player) {
        return players.remove(player);
    }

    public void clearPlayers() {
        players.clear();
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
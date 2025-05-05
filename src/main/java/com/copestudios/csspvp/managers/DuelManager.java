package com.copestudios.csspvp.managers;

import com.copestudios.csspvp.CSSPVP;
import com.copestudios.csspvp.arena.Arena;
import com.copestudios.csspvp.config.ZoneConfig;
import com.copestudios.csspvp.duel.Duel;
import com.copestudios.csspvp.duel.DuelZone;
import com.copestudios.csspvp.duel.ZoneSettings;
import com.copestudios.csspvp.utils.MessageId;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class DuelManager {

    private final CSSPVP plugin;
    private final ZoneConfig zoneConfig;
    private final Map<String, DuelZone> zones;
    private final Map<UUID, Duel> activeDuels;
    private final Map<UUID, UUID> pendingRequests;
    private final Map<UUID, BukkitTask> requestTasks;
    private final Map<UUID, BukkitTask> countdownTasks;

    public DuelManager(CSSPVP plugin) {
        this.plugin = plugin;
        this.zoneConfig = new ZoneConfig(plugin);
        this.zones = new HashMap<>();
        this.activeDuels = new HashMap<>();
        this.pendingRequests = new HashMap<>();
        this.requestTasks = new HashMap<>();
        this.countdownTasks = new HashMap<>();

        loadZones();
    }

    private void loadZones() {
        List<DuelZone> loadedZones = zoneConfig.loadZones();
        for (DuelZone zone : loadedZones) {
            zones.put(zone.getName().toLowerCase(), zone);
        }
    }

    public void saveZones() {
        for (DuelZone zone : zones.values()) {
            zoneConfig.saveZone(zone);
        }
    }

    public boolean createZone(String name, List<Location> corners, Location spawn1, Location spawn2) {
        String lowerName = name.toLowerCase();
        if (zones.containsKey(lowerName)) {
            return false;
        }

        DuelZone zone = new DuelZone(name, corners, spawn1, spawn2, new ZoneSettings(false, 30, 15));
        zones.put(lowerName, zone);
        zoneConfig.saveZone(zone);

        return true;
    }

    public boolean deleteZone(String name) {
        String lowerName = name.toLowerCase();
        if (!zones.containsKey(lowerName)) {
            return false;
        }

        DuelZone zone = zones.get(lowerName);
        if (zone.isInUse()) {
            // Stop any duels in this zone
            for (Map.Entry<UUID, Duel> entry : new HashMap<>(activeDuels).entrySet()) {
                if (entry.getValue().getZone().getName().equalsIgnoreCase(name)) {
                    stopDuel(entry.getValue(), true);
                }
            }
        }

        zones.remove(lowerName);
        return true;
    }

    public DuelZone getZone(String name) {
        return zones.get(name.toLowerCase());
    }

    public Set<String> getZoneNames() {
        return new HashSet<>(zones.keySet());
    }

    public DuelZone getAvailableZone() {
        for (DuelZone zone : zones.values()) {
            if (!zone.isInUse()) {
                return zone;
            }
        }
        return null;
    }

    public boolean isInDuel(Player player) {
        return activeDuels.containsKey(player.getUniqueId());
    }

    public Duel getPlayerDuel(Player player) {
        return activeDuels.get(player.getUniqueId());
    }

    public void requestDuel(Player sender, Player target) {
        if (isInDuel(sender) || isInDuel(target)) {
            sender.sendMessage(MessageId.get("duel.already-in-duel"));
            return;
        }

        UUID senderId = sender.getUniqueId();
        UUID targetId = target.getUniqueId();

        // Cancel any existing request from this player
        if (pendingRequests.containsKey(senderId)) {
            cancelRequest(senderId);
        }

        // Send duel request
        pendingRequests.put(senderId, targetId);

        // Create timeout task
        int timeout = plugin.getConfigManager().getConfig().getInt("duel.request-timeout", 60);
        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (pendingRequests.containsKey(senderId) && pendingRequests.get(senderId).equals(targetId)) {
                pendingRequests.remove(senderId);
                requestTasks.remove(senderId);

                Player senderPlayer = plugin.getServer().getPlayer(senderId);
                Player targetPlayer = plugin.getServer().getPlayer(targetId);

                if (senderPlayer != null) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("player", targetPlayer != null ? targetPlayer.getName() : "target");
                    senderPlayer.sendMessage(MessageId.get("duel.request.timeout", placeholders));
                }
            }
        }, timeout * 20L);

        requestTasks.put(senderId, task);

        // Send messages
        Map<String, String> senderPlaceholders = new HashMap<>();
        senderPlaceholders.put("player", target.getName());
        sender.sendMessage(MessageId.get("duel.request.sent", senderPlaceholders));

        Map<String, String> targetPlaceholders = new HashMap<>();
        targetPlaceholders.put("player", sender.getName());
        target.sendMessage(MessageId.get("duel.request.received", targetPlaceholders));
    }

    public void acceptDuel(Player player) {
        UUID playerId = player.getUniqueId();

        // Find the request
        UUID requesterId = null;
        for (Map.Entry<UUID, UUID> entry : pendingRequests.entrySet()) {
            if (entry.getValue().equals(playerId)) {
                requesterId = entry.getKey();
                break;
            }
        }

        if (requesterId == null) {
            player.sendMessage(MessageId.get("duel.no-request"));
            return;
        }

        Player requester = plugin.getServer().getPlayer(requesterId);
        if (requester == null) {
            player.sendMessage(MessageId.get("duel.requester-offline"));
            pendingRequests.remove(requesterId);
            if (requestTasks.containsKey(requesterId)) {
                requestTasks.remove(requesterId).cancel();
            }
            return;
        }

        // Cancel request timeout
        if (requestTasks.containsKey(requesterId)) {
            requestTasks.remove(requesterId).cancel();
        }

        // Remove from pending requests
        pendingRequests.remove(requesterId);

        // Find an available zone
        DuelZone zone = getAvailableZone();
        if (zone == null) {
            player.sendMessage(MessageId.get("duel.no-zones"));
            requester.sendMessage(MessageId.get("duel.no-zones"));
            return;
        }

        // Start duel
        startDuel(requester, player, zone);
    }

    public void cancelRequest(UUID playerId) {
        if (pendingRequests.containsKey(playerId)) {
            UUID targetId = pendingRequests.remove(playerId);

            // Cancel timeout task
            if (requestTasks.containsKey(playerId)) {
                requestTasks.remove(playerId).cancel();
            }

            // Notify players
            Player sender = plugin.getServer().getPlayer(playerId);
            Player target = plugin.getServer().getPlayer(targetId);

            if (sender != null) {
                sender.sendMessage(MessageId.get("duel.request.cancelled"));
            }

            if (target != null) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("player", sender != null ? sender.getName() : "requester");
                target.sendMessage(MessageId.get("duel.request.cancelled-target", placeholders));
            }
        }
    }

    public void startDuel(Player player1, Player player2, DuelZone zone) {
        // Mark zone as in use
        zone.setInUse(true);

        // Save original locations
        saveDuelLocations(player1, player2);

        // Create duel
        Duel duel = new Duel(player1, player2, zone);
        activeDuels.put(player1.getUniqueId(), duel);
        activeDuels.put(player2.getUniqueId(), duel);

        // Teleport players to zone spawns
        player1.teleport(zone.getSpawnLocation1());
        player2.teleport(zone.getSpawnLocation2());

        // Handle kit if players are in arena
        ArenaManager arenaManager = plugin.getArenaManager();
        boolean useArenaInventory = plugin.getConfigManager().getConfig().getBoolean("duel.use-arena-inventory", true);

        if (useArenaInventory) {
            Arena player1Arena = arenaManager.getPlayerArena(player1);
            Arena player2Arena = arenaManager.getPlayerArena(player2);

            if (player1Arena == null && player2Arena == null) {
                // Use default kit
                String defaultKit = plugin.getConfigManager().getConfig().getString("arena.default-kit", "default");
                ItemStack[] kit = plugin.getKitManager().getKit(defaultKit);
                if (kit != null) {
                    player1.getInventory().setContents(kit);
                    player2.getInventory().setContents(kit);
                }
            }
            // If either player is in an arena, they keep their current inventory
        }

        // Start countdown
        startDuelCountdown(duel);
    }

    private void startDuelCountdown(Duel duel) {
        final Player player1 = plugin.getServer().getPlayer(duel.getPlayer1());
        final Player player2 = plugin.getServer().getPlayer(duel.getPlayer2());

        if (player1 == null || player2 == null) {
            stopDuel(duel, true);
            return;
        }

        duel.setState(Duel.DuelState.COUNTDOWN);

        final int countdown = plugin.getConfigManager().getConfig().getInt("duel.countdown", 10);
        final int[] seconds = {countdown};

        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (seconds[0] > 0) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("seconds", String.valueOf(seconds[0]));

                player1.sendMessage(MessageId.get("duel.countdown", placeholders));
                player2.sendMessage(MessageId.get("duel.countdown", placeholders));

                seconds[0]--;
            } else {
                // Start the duel
                player1.sendMessage(MessageId.get("duel.started"));
                player2.sendMessage(MessageId.get("duel.started"));

                duel.setState(Duel.DuelState.ACTIVE);

                if (countdownTasks.containsKey(duel.getPlayer1())) {
                    countdownTasks.remove(duel.getPlayer1()).cancel();
                }
            }
        }, 0L, 20L);

        countdownTasks.put(duel.getPlayer1(), task);
    }

    public void endDuel(Duel duel, UUID winnerId) {
        if (duel == null) {
            return;
        }

        duel.setState(Duel.DuelState.FINISHED);

        Player winner = plugin.getServer().getPlayer(winnerId);
        UUID loserId = duel.getOpponent(winnerId);
        Player loser = plugin.getServer().getPlayer(loserId);

        if (winner != null && loser != null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", winner.getName());
            placeholders.put("opponent", loser.getName());

            // Broadcast victory message
            plugin.getServer().broadcastMessage(MessageId.get("duel.won", placeholders));

            // Teleport winner back
            winner.teleport(duel.getPlayer1().equals(winnerId) ? duel.getPlayer1PrevLocation() : duel.getPlayer2PrevLocation());

            // Set loser to spectator and teleport back
            loser.setGameMode(GameMode.SPECTATOR);
            loser.teleport(duel.getPlayer1().equals(loserId) ? duel.getPlayer1PrevLocation() : duel.getPlayer2PrevLocation());
        }

        // Remove duel
        cleanupDuel(duel);
    }

    public void stopDuel(Duel duel, boolean silent) {
        if (duel == null) {
            return;
        }

        Player player1 = plugin.getServer().getPlayer(duel.getPlayer1());
        Player player2 = plugin.getServer().getPlayer(duel.getPlayer2());

        // Teleport players back
        if (player1 != null) {
            player1.teleport(duel.getPlayer1PrevLocation());
            if (!silent) {
                player1.sendMessage(MessageId.get("duel.stopped"));
            }
        }

        if (player2 != null) {
            player2.teleport(duel.getPlayer2PrevLocation());
            if (!silent) {
                player2.sendMessage(MessageId.get("duel.stopped"));
            }
        }

        // Cleanup
        cleanupDuel(duel);
    }

    private void cleanupDuel(Duel duel) {
        // Remove countdown task if exists
        if (countdownTasks.containsKey(duel.getPlayer1())) {
            countdownTasks.remove(duel.getPlayer1()).cancel();
        }

        // Remove duel
        activeDuels.remove(duel.getPlayer1());
        activeDuels.remove(duel.getPlayer2());

        // Free up zone
        duel.getZone().setInUse(false);
    }
    public void addDuel(UUID playerId, Duel duel) {
        activeDuels.put(playerId, duel);
    }
    public void forfeitDuel(Player player) {
        UUID playerId = player.getUniqueId();
        Duel duel = activeDuels.get(playerId);

        if (duel == null) {
            player.sendMessage(MessageId.get("duel.not-in"));
            return;
        }

        // Announce forfeit
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", player.getName());
        plugin.getServer().broadcastMessage(MessageId.get("duel.forfeited", placeholders));

        // End duel with opponent as winner
        endDuel(duel, duel.getOpponent(playerId));
    }
    public void saveDuelLocations(Player player1, Player player2) {
        // Save player locations in dueldata.yml
        UUID player1Id = player1.getUniqueId();
        UUID player2Id = player2.getUniqueId();

        plugin.getConfigManager().getDuelData().set("locations." + player1Id + ".world", player1.getWorld().getName());
        plugin.getConfigManager().getDuelData().set("locations." + player1Id + ".x", player1.getLocation().getX());
        plugin.getConfigManager().getDuelData().set("locations." + player1Id + ".y", player1.getLocation().getY());
        plugin.getConfigManager().getDuelData().set("locations." + player1Id + ".z", player1.getLocation().getZ());
        plugin.getConfigManager().getDuelData().set("locations." + player1Id + ".yaw", player1.getLocation().getYaw());
        plugin.getConfigManager().getDuelData().set("locations." + player1Id + ".pitch", player1.getLocation().getPitch());

        plugin.getConfigManager().getDuelData().set("locations." + player2Id + ".world", player2.getWorld().getName());
        plugin.getConfigManager().getDuelData().set("locations." + player2Id + ".x", player2.getLocation().getX());
        plugin.getConfigManager().getDuelData().set("locations." + player2Id + ".y", player2.getLocation().getY());
        plugin.getConfigManager().getDuelData().set("locations." + player2Id + ".z", player2.getLocation().getZ());
        plugin.getConfigManager().getDuelData().set("locations." + player2Id + ".yaw", player2.getLocation().getYaw());
        plugin.getConfigManager().getDuelData().set("locations." + player2Id + ".pitch", player2.getLocation().getPitch());

        plugin.getConfigManager().saveDuelData();
    }

    public void saveDuelData() {
        plugin.getConfigManager().saveDuelData();
    }

    public void cleanUp() {
        // Cancel all tasks
        for (BukkitTask task : requestTasks.values()) {
            task.cancel();
        }
        requestTasks.clear();

        for (BukkitTask task : countdownTasks.values()) {
            task.cancel();
        }
        countdownTasks.clear();

        // Clear all duels
        for (Duel duel : new HashSet<>(activeDuels.values())) {
            duel.getZone().setInUse(false);
        }

        activeDuels.clear();
        pendingRequests.clear();
    }
}
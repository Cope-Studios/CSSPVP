package com.copestudios.csspvp.managers;

import com.copestudios.csspvp.CSSPVP;
import com.copestudios.csspvp.config.ArenaConfig;
import com.copestudios.csspvp.duel.Duel;
import com.copestudios.csspvp.duel.DuelZone;
import com.copestudios.csspvp.utils.MessageId;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class RandomDuelManager {

    private final CSSPVP plugin;
    private final Map<String, BukkitTask> countdownTasks;
    private final Map<UUID, String> randomDuelPlayers;

    public RandomDuelManager(CSSPVP plugin) {
        this.plugin = plugin;
        this.countdownTasks = new HashMap<>();
        this.randomDuelPlayers = new HashMap<>();
    }

    public void startRandomDuel(String arenaName) {
        ArenaConfig arenaConfig = new ArenaConfig(plugin);
        List<String> players = arenaConfig.getPlayersInCache(arenaName.toLowerCase());

        if (players.size() < 2) {
            Bukkit.getConsoleSender().sendMessage(MessageId.get("random-duel.not-enough", "name", arenaName));
            return;
        }

        // Shuffle players and pick 2
        Collections.shuffle(players);

        Player player1 = Bukkit.getPlayer(players.get(0));
        Player player2 = Bukkit.getPlayer(players.get(1));

        if (player1 == null || player2 == null) {
            Bukkit.getConsoleSender().sendMessage(MessageId.get("random-duel.player-offline"));
            return;
        }

        // Check if players are already in a duel
        if (plugin.getDuelManager().isInDuel(player1) || plugin.getDuelManager().isInDuel(player2)) {
            Bukkit.getConsoleSender().sendMessage(MessageId.get("random-duel.player-in-duel"));
            return;
        }

        // Find an available zone
        DuelZone zone = plugin.getDuelManager().getAvailableZone();
        if (zone == null) {
            player1.sendMessage(MessageId.get("duel.no-zones"));
            player2.sendMessage(MessageId.get("duel.no-zones"));
            Bukkit.getConsoleSender().sendMessage(MessageId.get("duel.no-zones"));
            return;
        }

        // Notify players
        Map<String, String> player1Placeholders = new HashMap<>();
        player1Placeholders.put("player", player2.getName());
        player1.sendMessage(MessageId.get("random-duel.selected", player1Placeholders));

        Map<String, String> player2Placeholders = new HashMap<>();
        player2Placeholders.put("player", player1.getName());
        player2.sendMessage(MessageId.get("random-duel.selected", player2Placeholders));

        // Mark players as in random duel
        randomDuelPlayers.put(player1.getUniqueId(), arenaName);
        randomDuelPlayers.put(player2.getUniqueId(), arenaName);

        // Start random duel
        startRandomDuelCountdown(player1, player2, zone, arenaName);
    }

    private void startRandomDuelCountdown(Player player1, Player player2, DuelZone zone, String arenaName) {
        // Save original locations
        plugin.getDuelManager().saveDuelLocations(player1, player2);

        // Mark zone as in use
        zone.setInUse(true);

        final int countdown = plugin.getConfigManager().getConfig().getInt("random-duel.countdown", 5);
        final int[] seconds = {countdown};

        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (seconds[0] > 0) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("seconds", String.valueOf(seconds[0]));

                player1.sendMessage(MessageId.get("random-duel.countdown", placeholders));
                player2.sendMessage(MessageId.get("random-duel.countdown", placeholders));

                seconds[0]--;
            } else {
                // Start the duel
                player1.sendMessage(MessageId.get("random-duel.started"));
                player2.sendMessage(MessageId.get("random-duel.started"));

                // Teleport players
                player1.teleport(zone.getSpawnLocation1());
                player2.teleport(zone.getSpawnLocation2());

                // Create duel
                Duel duel = new Duel(player1, player2, zone);
                duel.setState(Duel.DuelState.ACTIVE);
                plugin.getDuelManager().addDuel(player1.getUniqueId(), duel);
                plugin.getDuelManager().addDuel(player2.getUniqueId(), duel);

                // Cancel countdown task
                if (countdownTasks.containsKey(arenaName)) {
                    countdownTasks.remove(arenaName).cancel();
                }
            }
        }, 0L, 20L);

        countdownTasks.put(arenaName, task);
    }

    public void stopRandomDuel(String arenaName) {
        // Find players in this random duel
        List<UUID> playersToRemove = new ArrayList<>();

        for (Map.Entry<UUID, String> entry : randomDuelPlayers.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(arenaName)) {
                playersToRemove.add(entry.getKey());

                Player player = Bukkit.getPlayer(entry.getKey());
                if (player != null) {
                    Duel duel = plugin.getDuelManager().getPlayerDuel(player);
                    if (duel != null) {
                        plugin.getDuelManager().stopDuel(duel, false);
                    }
                }
            }
        }

        // Remove players from random duel
        for (UUID playerId : playersToRemove) {
            randomDuelPlayers.remove(playerId);
        }

        // Cancel countdown task
        if (countdownTasks.containsKey(arenaName)) {
            countdownTasks.remove(arenaName).cancel();
        }
    }

    public boolean isInRandomDuel(Player player) {
        return randomDuelPlayers.containsKey(player.getUniqueId());
    }

    public String getRandomDuelArena(Player player) {
        return randomDuelPlayers.get(player.getUniqueId());
    }

    public void forfeitRandomDuel(Player player) {
        UUID playerId = player.getUniqueId();

        if (!randomDuelPlayers.containsKey(playerId)) {
            player.sendMessage(MessageId.get("duel.not-in"));
            return;
        }

        Duel duel = plugin.getDuelManager().getPlayerDuel(player);
        if (duel == null) {
            // Player is only in countdown phase
            String arenaName = randomDuelPlayers.get(playerId);
            randomDuelPlayers.remove(playerId);

            // Find opponent
            UUID opponentId = null;
            for (Map.Entry<UUID, String> entry : randomDuelPlayers.entrySet()) {
                if (entry.getValue().equalsIgnoreCase(arenaName)) {
                    opponentId = entry.getKey();
                    break;
                }
            }

            if (opponentId != null) {
                Player opponent = Bukkit.getPlayer(opponentId);
                if (opponent != null) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("player", player.getName());
                    opponent.sendMessage(MessageId.get("duel.forfeited", placeholders));
                }
                randomDuelPlayers.remove(opponentId);
            }

            // Cancel countdown
            if (countdownTasks.containsKey(arenaName)) {
                countdownTasks.remove(arenaName).cancel();
            }

            return;
        }

        // Announce forfeit
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", player.getName());
        plugin.getServer().broadcastMessage(MessageId.get("duel.forfeited", placeholders));

        // End duel with opponent as winner
        UUID opponentId = duel.getOpponent(playerId);

        // Remove from random duel
        randomDuelPlayers.remove(playerId);
        randomDuelPlayers.remove(opponentId);

        // End actual duel
        plugin.getDuelManager().endDuel(duel, opponentId);
    }

    public void cleanUp() {
        // Cancel all tasks
        for (BukkitTask task : countdownTasks.values()) {
            task.cancel();
        }
        countdownTasks.clear();
        randomDuelPlayers.clear();
    }
}
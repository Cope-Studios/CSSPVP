package com.copestudios.csspvp.duel;

import com.copestudios.csspvp.CSSPVP;
import com.copestudios.csspvp.arena.Arena;
import com.copestudios.csspvp.messages.MessageManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.*;

public class DuelManager implements Listener {
    private final CSSPVP plugin;
    private final Map<UUID, Map<UUID, DuelRequest>> pendingDuels; // sender -> (receiver -> request)
    private final Map<UUID, UUID> activeDuels; // player -> opponent
    private final Map<UUID, Location> lastLocations; // player -> last location before duel

    public DuelManager(CSSPVP plugin) {
        this.plugin = plugin;
        this.pendingDuels = new HashMap<>();
        this.activeDuels = new HashMap<>();
        this.lastLocations = new HashMap<>();

        // Register this as a listener for player death events
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID playerId = player.getUniqueId();

        // Check if this player is in an active duel from cssrduel command
        if (activeDuels.containsKey(playerId)) {
            UUID opponentId = activeDuels.get(playerId);
            Player opponent = plugin.getServer().getPlayer(opponentId);

            if (opponent != null) {
                // Announce the winner
                plugin.getServer().broadcastMessage(
                        plugin.colorizeString("&6&l" + opponent.getName() + " &a&lhas won the duel against &c&l" + player.getName() + "!")
                );

                // Return the winner to their previous location
                Location lastLocation = lastLocations.get(opponentId);
                if (lastLocation != null) {
                    opponent.teleport(lastLocation);
                    opponent.sendMessage(plugin.colorizeString("&aYou've been teleported back to your previous location."));
                }

                // Clean up
                activeDuels.remove(playerId);
                activeDuels.remove(opponentId);
                lastLocations.remove(playerId);
                lastLocations.remove(opponentId);

                // Remove from arena
                Arena arena = plugin.getArenaManager().getPlayerArena(opponent);
                if (arena != null) {
                    arena.removePlayer(opponent);
                }
            }
        }
    }

    public static class DuelRequest {
        private final UUID sender;
        private final UUID receiver;
        private final String arenaName;
        private final long timestamp;

        public DuelRequest(UUID sender, UUID receiver, String arenaName) {
            this.sender = sender;
            this.receiver = receiver;
            this.arenaName = arenaName;
            this.timestamp = System.currentTimeMillis();
        }

        public UUID getSender() {
            return sender;
        }

        public UUID getReceiver() {
            return receiver;
        }

        public String getArenaName() {
            return arenaName;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public boolean isExpired() {
            // Expire after 60 seconds
            return System.currentTimeMillis() - timestamp > 60000;
        }
    }

    public boolean sendDuelRequest(Player sender, Player receiver, String arenaName) {
        return sendDuelRequest(sender, receiver, arenaName, false);
    }

    public boolean sendDuelRequest(Player sender, Player receiver, String arenaName, boolean ignoreArenaCheck) {
        // Check if arena exists
        Arena arena = plugin.getArenaManager().getArena(arenaName);
        if (arena == null || !arena.isSetup()) {
            return false;
        }

        // Check if players are already in arena (unless we're ignoring this check)
        if (!ignoreArenaCheck &&
                (plugin.getArenaManager().isPlayerInArena(sender) ||
                        plugin.getArenaManager().isPlayerInArena(receiver))) {
            return false;
        }

        // Check if already has pending request
        Map<UUID, DuelRequest> senderRequests = pendingDuels.computeIfAbsent(
                sender.getUniqueId(), k -> new HashMap<>());

        if (senderRequests.containsKey(receiver.getUniqueId())) {
            // Request already exists
            return false;
        }

        // Create and store request
        DuelRequest request = new DuelRequest(
                sender.getUniqueId(),
                receiver.getUniqueId(),
                arenaName
        );

        senderRequests.put(receiver.getUniqueId(), request);

        // Send messages
        sender.sendMessage(plugin.getMessageManager().getMessage(
                MessageManager.DUEL_SENT,
                "player", receiver.getName()
        ));

        receiver.sendMessage(plugin.getMessageManager().getMessage(
                MessageManager.DUEL_RECEIVED,
                "player", sender.getName(),
                "arena", arenaName
        ));

        // Schedule expiration
        new BukkitRunnable() {
            @Override
            public void run() {
                // Check if request still exists and is expired
                Map<UUID, DuelRequest> requests = pendingDuels.get(sender.getUniqueId());
                if (requests != null) {
                    DuelRequest req = requests.get(receiver.getUniqueId());
                    if (req != null && req.isExpired()) {
                        requests.remove(receiver.getUniqueId());

                        // If sender has no more requests, remove from map
                        if (requests.isEmpty()) {
                            pendingDuels.remove(sender.getUniqueId());
                        }

                        // Notify players if they're still online
                        Player s = plugin.getServer().getPlayer(sender.getUniqueId());
                        Player r = plugin.getServer().getPlayer(receiver.getUniqueId());

                        if (s != null) {
                            s.sendMessage(plugin.getMessageManager().getMessage(
                                    MessageManager.DUEL_EXPIRED,
                                    "player", receiver.getName()
                            ));
                        }

                        if (r != null) {
                            r.sendMessage(plugin.getMessageManager().getMessage(
                                    MessageManager.DUEL_EXPIRED,
                                    "player", sender.getName()
                            ));
                        }
                    }
                }
            }
        }.runTaskLater(plugin, 1200L); // 60 seconds

        return true;
    }

    public boolean acceptDuelRequest(Player player) {
        return acceptDuelRequest(player, false);
    }

    public boolean acceptDuelRequest(Player player, boolean ignoreArenaCheck) {
        // Find requests where this player is the receiver
        DuelRequest acceptedRequest = null;
        UUID senderUUID = null;

        for (Map.Entry<UUID, Map<UUID, DuelRequest>> entry : pendingDuels.entrySet()) {
            Map<UUID, DuelRequest> requests = entry.getValue();
            DuelRequest request = requests.get(player.getUniqueId());

            if (request != null && !request.isExpired()) {
                acceptedRequest = request;
                senderUUID = entry.getKey();
                break;
            }
        }

        if (acceptedRequest == null || senderUUID == null) {
            return false;
        }

        // Remove request
        pendingDuels.get(senderUUID).remove(player.getUniqueId());
        if (pendingDuels.get(senderUUID).isEmpty()) {
            pendingDuels.remove(senderUUID);
        }

        // Start duel
        Player sender = plugin.getServer().getPlayer(senderUUID);
        if (sender == null || !sender.isOnline()) {
            return false;
        }

        // Get arena
        Arena arena = plugin.getArenaManager().getArena(acceptedRequest.getArenaName());
        if (arena == null || !arena.isSetup()) {
            return false;
        }

        // Remove players from current arenas if needed and if ignoreArenaCheck is true
        if (ignoreArenaCheck) {
            Arena senderArena = plugin.getArenaManager().getPlayerArena(sender);
            if (senderArena != null) {
                senderArena.removePlayer(sender);
            }

            Arena receiverArena = plugin.getArenaManager().getPlayerArena(player);
            if (receiverArena != null) {
                receiverArena.removePlayer(player);
            }
        }

        // Notify players
        player.sendMessage(plugin.getMessageManager().getMessage(
                MessageManager.DUEL_ACCEPTED,
                "player", sender.getName()
        ));

        sender.sendMessage(plugin.getMessageManager().getMessage(
                MessageManager.DUEL_ACCEPTED,
                "player", player.getName()
        ));

        // Add to arena
        arena.addPlayer(sender);
        arena.addPlayer(player);

        // Start arena
        arena.startArena();

        return true;
    }

    public boolean declineDuelRequest(Player player) {
        // Find requests where this player is the receiver
        DuelRequest declinedRequest = null;
        UUID senderUUID = null;

        for (Map.Entry<UUID, Map<UUID, DuelRequest>> entry : pendingDuels.entrySet()) {
            Map<UUID, DuelRequest> requests = entry.getValue();
            DuelRequest request = requests.get(player.getUniqueId());

            if (request != null && !request.isExpired()) {
                declinedRequest = request;
                senderUUID = entry.getKey();
                break;
            }
        }

        if (declinedRequest == null || senderUUID == null) {
            return false;
        }

        // Remove request
        pendingDuels.get(senderUUID).remove(player.getUniqueId());
        if (pendingDuels.get(senderUUID).isEmpty()) {
            pendingDuels.remove(senderUUID);
        }

        // Notify sender if online
        Player sender = plugin.getServer().getPlayer(senderUUID);
        if (sender != null && sender.isOnline()) {
            sender.sendMessage(plugin.getMessageManager().getMessage(
                    MessageManager.DUEL_DECLINED,
                    "player", player.getName()
            ));
        }

        // Notify receiver
        player.sendMessage(plugin.getMessageManager().getMessage(
                MessageManager.DUEL_DECLINED,
                "player", sender != null ? sender.getName() : "Unknown"
        ));

        return true;
    }

    public boolean hasOutgoingRequest(Player sender, Player receiver) {
        Map<UUID, DuelRequest> requests = pendingDuels.get(sender.getUniqueId());
        if (requests == null) {
            return false;
        }

        DuelRequest request = requests.get(receiver.getUniqueId());
        return request != null && !request.isExpired();
    }

    public boolean hasIncomingRequest(Player receiver) {
        for (Map<UUID, DuelRequest> requests : pendingDuels.values()) {
            DuelRequest request = requests.get(receiver.getUniqueId());
            if (request != null && !request.isExpired()) {
                return true;
            }
        }

        return false;
    }

    public void cleanupExpiredRequests() {
        // Remove all expired requests
        Iterator<Map.Entry<UUID, Map<UUID, DuelRequest>>> it = pendingDuels.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<UUID, Map<UUID, DuelRequest>> entry = it.next();
            Map<UUID, DuelRequest> requests = entry.getValue();

            requests.entrySet().removeIf(req -> req.getValue().isExpired());

            if (requests.isEmpty()) {
                it.remove();
            }
        }
    }

    public boolean startRandomDuel(String arenaName) {
        // Get the arena
        Arena arena = plugin.getArenaManager().getArena(arenaName);
        if (arena == null || !arena.isSetup()) {
            return false;
        }

        // Get all online players
        List<Player> eligiblePlayers = new ArrayList<>();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            // Don't include players already in arenas
            if (!plugin.getArenaManager().isPlayerInArena(player)) {
                eligiblePlayers.add(player);
            }
        }

        // Need at least 2 players
        if (eligiblePlayers.size() < 2) {
            return false;
        }

        // Shuffle and take first 2
        Collections.shuffle(eligiblePlayers);
        Player player1 = eligiblePlayers.get(0);
        Player player2 = eligiblePlayers.get(1);

        // Send individual messages to selected players
        player1.sendMessage(plugin.colorizeString("&aYou have been selected for a random duel against &e" +
                player2.getName() + " &ain arena &e" + arenaName + "&a!"));

        player2.sendMessage(plugin.colorizeString("&aYou have been selected for a random duel against &e" +
                player1.getName() + " &ain arena &e" + arenaName + "&a!"));

        // Broadcast message
        String message = plugin.getMessageManager().getMessageString(
                MessageManager.DUEL_RANDOM_SELECTED,
                "player1", player1.getName(),
                "player2", player2.getName(),
                "arena", arenaName
        );
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            player.sendMessage(message);
        }

        // Add to arena
        arena.addPlayer(player1);
        arena.addPlayer(player2);

        // Start arena
        arena.startArena();

        return true;
    }

    public boolean startRandomDuel(String arenaName, Player excludePlayer) {
        // Get the arena
        Arena arena = plugin.getArenaManager().getArena(arenaName);
        if (arena == null || !arena.isSetup()) {
            return false;
        }

        // Get all online players except the excluded one
        List<Player> eligiblePlayers = new ArrayList<>();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            // Don't include excluded player or players already in arenas
            if (!player.equals(excludePlayer) && !plugin.getArenaManager().isPlayerInArena(player)) {
                eligiblePlayers.add(player);
            }
        }

        // Need at least 2 players
        if (eligiblePlayers.size() < 2) {
            return false;
        }

        // Shuffle and take first 2
        Collections.shuffle(eligiblePlayers);
        Player player1 = eligiblePlayers.get(0);
        Player player2 = eligiblePlayers.get(1);

        // Send individual messages to selected players
        player1.sendMessage(plugin.colorizeString("&aYou have been selected for a random duel against &e" +
                player2.getName() + " &ain arena &e" + arenaName + "&a!"));

        player2.sendMessage(plugin.colorizeString("&aYou have been selected for a random duel against &e" +
                player1.getName() + " &ain arena &e" + arenaName + "&a!"));

        // Broadcast message
        String message = plugin.getMessageManager().getMessageString(
                MessageManager.DUEL_RANDOM_SELECTED,
                "player1", player1.getName(),
                "player2", player2.getName(),
                "arena", arenaName
        );
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            player.sendMessage(message);
        }

        // Add to arena
        arena.addPlayer(player1);
        arena.addPlayer(player2);

        // Start arena
        arena.startArena();

        return true;
    }

    public boolean startRandomDuelFromArena(Arena sourceArena, Arena targetArena, Map<UUID, Location> previousLocations) {
        if (sourceArena == null || targetArena == null || !targetArena.isSetup()) {
            return false;
        }

        // Get a list of players from the source arena
        List<Player> playersInSourceArena = new ArrayList<>();
        for (UUID playerId : sourceArena.getParticipants().keySet()) {
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null && player.isOnline()) {
                playersInSourceArena.add(player);
            }
        }

        // Need at least 2 players in the source arena
        if (playersInSourceArena.size() < 2) {
            return false;
        }

        // Shuffle and take first 2
        Collections.shuffle(playersInSourceArena);
        Player player1 = playersInSourceArena.get(0);
        Player player2 = playersInSourceArena.get(1);

        // Store their current locations
        lastLocations.put(player1.getUniqueId(), player1.getLocation());
        lastLocations.put(player2.getUniqueId(), player2.getLocation());

        // Add to the map for tracking in the death event
        activeDuels.put(player1.getUniqueId(), player2.getUniqueId());
        activeDuels.put(player2.getUniqueId(), player1.getUniqueId());

        // Also add to the external tracking map if provided
        if (previousLocations != null) {
            previousLocations.put(player1.getUniqueId(), player1.getLocation());
            previousLocations.put(player2.getUniqueId(), player2.getLocation());
        }

        // Remove players from source arena
        sourceArena.removePlayer(player1);
        sourceArena.removePlayer(player2);

        // Send individual messages to selected players
        player1.sendMessage(plugin.colorizeString("&aYou have been selected for a random duel against &e" +
                player2.getName() + " &ain arena &e" + targetArena.getName() + "&a!"));

        player2.sendMessage(plugin.colorizeString("&aYou have been selected for a random duel against &e" +
                player1.getName() + " &ain arena &e" + targetArena.getName() + "&a!"));

        // Broadcast message
        String message = plugin.colorizeString("&aRandom duel: &e" + player1.getName() +
                " &avs &e" + player2.getName() + " &ain arena &e" + targetArena.getName() + "&a!");

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            player.sendMessage(message);
        }

        // Add to target arena
        targetArena.addPlayer(player1);
        targetArena.addPlayer(player2);

        // Start target arena
        targetArena.startArena();

        return true;
    }
}
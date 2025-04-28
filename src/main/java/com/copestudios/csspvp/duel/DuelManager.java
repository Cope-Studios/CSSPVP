package com.copestudios.csspvp.duel;

import com.copestudios.csspvp.CSSPVP;
import com.copestudios.csspvp.arena.Arena;
import com.copestudios.csspvp.messages.MessageManager;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class DuelManager {
    private final CSSPVP plugin;
    private final Map<UUID, Map<UUID, DuelRequest>> pendingDuels; // sender -> (receiver -> request)

    public DuelManager(CSSPVP plugin) {
        this.plugin = plugin;
        this.pendingDuels = new HashMap<>();
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
        // Check if arena exists
        Arena arena = plugin.getArenaManager().getArena(arenaName);
        if (arena == null || !arena.isSetup()) {
            return false;
        }

        // Check if players are already in arena
        if (plugin.getArenaManager().isPlayerInArena(sender) ||
                plugin.getArenaManager().isPlayerInArena(receiver)) {
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

        // Broadcast message to all players individually
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

        // Broadcast message to all players individually
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
}
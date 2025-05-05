package com.copestudios.csspvp.listeners;

import com.copestudios.csspvp.CSSPVP;
import com.copestudios.csspvp.arena.Arena;
import com.copestudios.csspvp.arena.ArenaSettings;
import com.copestudios.csspvp.commands.CSPCommand;
import com.copestudios.csspvp.commands.CSDCommand;
import com.copestudios.csspvp.duel.ZoneSettings;
import com.copestudios.csspvp.utils.MessageId;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatListener implements Listener {

    private final CSSPVP plugin;

    public ChatListener(CSSPVP plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        String message = event.getMessage();

        // Handle arena creation
        CSPCommand cspCommand = (CSPCommand) plugin.getCommand("csp").getExecutor();

        if (cspCommand.isCreatingArena(playerId)) {
            event.setCancelled(true);

            if (!cspCommand.hasArenaName(playerId)) {
                // Set arena name
                handleArenaName(player, message, cspCommand);
            } else if (message.equalsIgnoreCase("here")) {
                // Mark corner or spawn point
                handleArenaLocation(player, cspCommand);
            } else {
                player.sendMessage(MessageId.get("arena.create.invalid"));
                player.sendMessage(MessageId.get("arena.create.type-here"));
            }

            return;
        }

        // Handle zone creation
        CSDCommand csdCommand = (CSDCommand) plugin.getCommand("csd").getExecutor();

        if (csdCommand.isCreatingZone(playerId)) {
            event.setCancelled(true);

            if (!csdCommand.hasZoneName(playerId)) {
                // Set zone name
                handleZoneName(player, message, csdCommand);
            } else if (message.equalsIgnoreCase("here")) {
                // Mark corner or spawn point
                handleZoneLocation(player, csdCommand);
            } else {
                player.sendMessage(MessageId.get("zone.create.invalid"));
                player.sendMessage(MessageId.get("zone.create.type-here"));
            }

            return;
        }

        // Handle arena setup
        if (cspCommand.isSettingUpArena(playerId)) {
            event.setCancelled(true);

            String arenaName = cspCommand.getSetupArenaName(playerId);
            Arena arena = plugin.getArenaManager().getArena(arenaName);

            if (arena == null) {
                cspCommand.completeArenaSetup(playerId);
                player.sendMessage(MessageId.get("arena.setup.error"));
                return;
            }

            ArenaSettings settings = arena.getSettings();

            // Lives setting
            if (settings.getLives() == 3 && !settings.hasInfiniteLives()) { // Default value, not yet set
                boolean handled = handleArenaLives(player, message, settings);
                if (handled) {
                    player.sendMessage(MessageId.get("arena.setup.drop-items"));
                }
            }
            // Drop items setting
            else if (settings.canDropItems() == false) { // Default value, not yet set
                boolean handled = handleArenaDropItems(player, message, settings);
                if (handled) {
                    player.sendMessage(MessageId.get("arena.setup.notch-apple"));
                }
            }
            // Notch apple delay setting
            else if (settings.getNotchAppleDelay() == 30) { // Default value, not yet set
                boolean handled = handleArenaNotchApple(player, message, settings);
                if (handled) {
                    player.sendMessage(MessageId.get("arena.setup.enderpearl"));
                }
            }
            // Enderpearl delay setting
            else if (settings.getEnderpearlDelay() == 15) { // Default value, not yet set
                boolean handled = handleArenaEnderpearl(player, message, settings);
                if (handled) {
                    player.sendMessage(MessageId.get("arena.setup.max-players"));
                }
            }
            // Max players setting
            else if (settings.getMaxPlayers() == 10) { // Default value, not yet set
                boolean handled = handleArenaMaxPlayers(player, message, settings);
                if (handled) {
                    // Save arena settings
                    plugin.getArenaManager().saveArenas();

                    // Complete setup
                    cspCommand.completeArenaSetup(playerId);

                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("name", arenaName);
                    player.sendMessage(MessageId.get("arena.setup.success", placeholders));
                }
            }

            return;
        }

        // Handle zone setup
        if (csdCommand.isSettingUpZone(playerId)) {
            event.setCancelled(true);

            String zoneName = csdCommand.getSetupZoneName(playerId);
            ZoneSettings settings = plugin.getDuelManager().getZone(zoneName).getSettings();

            // Drop items setting
            if (settings.canDropItems() == false) { // Default value, not yet set
                boolean handled = handleZoneDropItems(player, message, settings);
                if (handled) {
                    player.sendMessage(MessageId.get("zone.setup.notch-apple"));
                }
            }
            // Notch apple delay setting
            else if (settings.getNotchAppleDelay() == 30) { // Default value, not yet set
                boolean handled = handleZoneNotchApple(player, message, settings);
                if (handled) {
                    player.sendMessage(MessageId.get("zone.setup.enderpearl"));
                }
            }
            // Enderpearl delay setting
            else if (settings.getEnderpearlDelay() == 15) { // Default value, not yet set
                boolean handled = handleZoneEnderpearl(player, message, settings);
                if (handled) {
                    // Save zone settings
                    plugin.getDuelManager().saveZones();

                    // Complete setup
                    csdCommand.completeZoneSetup(playerId);

                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("name", zoneName);
                    player.sendMessage(MessageId.get("zone.setup.success", placeholders));
                }
            }

            return;
        }
    }

    private void handleArenaName(Player player, String message, CSPCommand cspCommand) {
        if (message.length() < 3 || message.length() > 16) {
            player.sendMessage(MessageId.get("arena.create.name-length"));
            return;
        }

        if (!message.matches("[a-zA-Z0-9_]+")) {
            player.sendMessage(MessageId.get("arena.create.name-format"));
            return;
        }

        if (plugin.getArenaManager().getArena(message) != null) {
            player.sendMessage(MessageId.get("arena.create.name-exists"));
            return;
        }

        cspCommand.setArenaName(player.getUniqueId(), message);
        player.sendMessage(MessageId.get("arena.create.corners", "number", "1"));
        player.sendMessage(MessageId.get("arena.create.type-here"));
    }

    private void handleArenaLocation(Player player, CSPCommand cspCommand) {
        UUID playerId = player.getUniqueId();
        int cornerCount = cspCommand.getArenaCornerCount(playerId);

        if (cornerCount < 4) {
            // Mark corner
            cspCommand.addArenaCorner(playerId, player.getLocation());

            cornerCount++;
            if (cornerCount < 4) {
                // Need more corners
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("number", String.valueOf(cornerCount + 1));
                player.sendMessage(MessageId.get("arena.create.corners", placeholders));
                player.sendMessage(MessageId.get("arena.create.type-here"));
            } else {
                // All corners marked, need spawn point
                player.sendMessage(MessageId.get("arena.create.spawn"));
                player.sendMessage(MessageId.get("arena.create.type-here"));
            }
        } else {
            // Set spawn point and complete arena creation
            boolean success = cspCommand.completeArenaCreation(playerId, player.getLocation());

            if (success) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("name", cspCommand.getArenaName(playerId));
                player.sendMessage(MessageId.get("arena.create.success", placeholders));
            } else {
                player.sendMessage(MessageId.get("arena.create.error"));
            }
        }
    }

    private void handleZoneName(Player player, String message, CSDCommand csdCommand) {
        if (message.length() < 3 || message.length() > 16) {
            player.sendMessage(MessageId.get("zone.create.name-length"));
            return;
        }

        if (!message.matches("[a-zA-Z0-9_]+")) {
            player.sendMessage(MessageId.get("zone.create.name-format"));
            return;
        }

        if (plugin.getDuelManager().getZone(message) != null) {
            player.sendMessage(MessageId.get("zone.create.name-exists"));
            return;
        }

        csdCommand.setZoneName(player.getUniqueId(), message);
        player.sendMessage(MessageId.get("zone.create.corners", "number", "1"));
        player.sendMessage(MessageId.get("zone.create.type-here"));
    }

    private void handleZoneLocation(Player player, CSDCommand csdCommand) {
        UUID playerId = player.getUniqueId();
        int cornerCount = csdCommand.getZoneCornerCount(playerId);

        if (cornerCount < 4) {
            // Mark corner
            csdCommand.addZoneCorner(playerId, player.getLocation());

            cornerCount++;
            if (cornerCount < 4) {
                // Need more corners
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("number", String.valueOf(cornerCount + 1));
                player.sendMessage(MessageId.get("zone.create.corners", placeholders));
                player.sendMessage(MessageId.get("zone.create.type-here"));
            } else {
                // All corners marked, need spawn point 1
                player.sendMessage(MessageId.get("zone.create.spawn1"));
                player.sendMessage(MessageId.get("zone.create.type-here"));
            }
        } else if (!csdCommand.hasSpawnPoint1(playerId)) {
            // Set spawn point 1
            csdCommand.setSpawnPoint1(playerId, player.getLocation());
            player.sendMessage(MessageId.get("zone.create.spawn2"));
            player.sendMessage(MessageId.get("zone.create.type-here"));
        } else {
            // Set spawn point 2 and complete zone creation
            boolean success = csdCommand.completeZoneCreation(playerId, player.getLocation());

            if (success) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("name", csdCommand.getZoneName(playerId));
                player.sendMessage(MessageId.get("zone.create.success", placeholders));
            } else {
                player.sendMessage(MessageId.get("zone.create.error"));
            }
        }
    }

    private boolean handleArenaLives(Player player, String message, ArenaSettings settings) {
        if (message.equalsIgnoreCase("infinite")) {
            settings.setInfiniteLives(true);
            player.sendMessage(MessageId.get("arena.setup.lives-set", "value", "infinite"));
            return true;
        }

        try {
            int lives = Integer.parseInt(message);
            if (lives < 1 || lives > 10) {
                player.sendMessage(MessageId.get("arena.setup.lives-range"));
                player.sendMessage(MessageId.get("arena.setup.lives"));
                return false;
            }

            settings.setLives(lives);
            player.sendMessage(MessageId.get("arena.setup.lives-set", "value", String.valueOf(lives)));
            return true;
        } catch (NumberFormatException e) {
            player.sendMessage(MessageId.get("arena.setup.lives-invalid"));
            player.sendMessage(MessageId.get("arena.setup.lives"));
            return false;
        }
    }

    private boolean handleArenaDropItems(Player player, String message, ArenaSettings settings) {
        if (message.equalsIgnoreCase("yes")) {
            settings.setDropItems(true);
            player.sendMessage(MessageId.get("arena.setup.drop-items-set", "value", "yes"));
            return true;
        } else if (message.equalsIgnoreCase("no")) {
            settings.setDropItems(false);
            player.sendMessage(MessageId.get("arena.setup.drop-items-set", "value", "no"));
            return true;
        } else {
            player.sendMessage(MessageId.get("arena.setup.drop-items-invalid"));
            player.sendMessage(MessageId.get("arena.setup.drop-items"));
            return false;
        }
    }

    private boolean handleArenaNotchApple(Player player, String message, ArenaSettings settings) {
        try {
            int delay = Integer.parseInt(message);
            if (delay < 0) {
                player.sendMessage(MessageId.get("arena.setup.notch-apple-invalid"));
                player.sendMessage(MessageId.get("arena.setup.notch-apple"));
                return false;
            }

            settings.setNotchAppleDelay(delay);
            player.sendMessage(MessageId.get("arena.setup.notch-apple-set", "value", String.valueOf(delay)));
            return true;
        } catch (NumberFormatException e) {
            player.sendMessage(MessageId.get("arena.setup.notch-apple-invalid"));
            player.sendMessage(MessageId.get("arena.setup.notch-apple"));
            return false;
        }
    }

    private boolean handleArenaEnderpearl(Player player, String message, ArenaSettings settings) {
        try {
            int delay = Integer.parseInt(message);
            if (delay < 0) {
                player.sendMessage(MessageId.get("arena.setup.enderpearl-invalid"));
                player.sendMessage(MessageId.get("arena.setup.enderpearl"));
                return false;
            }

            settings.setEnderpearlDelay(delay);
            player.sendMessage(MessageId.get("arena.setup.enderpearl-set", "value", String.valueOf(delay)));
            return true;
        } catch (NumberFormatException e) {
            player.sendMessage(MessageId.get("arena.setup.enderpearl-invalid"));
            player.sendMessage(MessageId.get("arena.setup.enderpearl"));
            return false;
        }
    }

    private boolean handleArenaMaxPlayers(Player player, String message, ArenaSettings settings) {
        if (message.equalsIgnoreCase("unlimited")) {
            settings.setMaxPlayers(-1);
            player.sendMessage(MessageId.get("arena.setup.max-players-set", "value", "unlimited"));
            return true;
        }

        try {
            int maxPlayers = Integer.parseInt(message);
            if (maxPlayers < 1 || maxPlayers > 100) {
                player.sendMessage(MessageId.get("arena.setup.max-players-range"));
                player.sendMessage(MessageId.get("arena.setup.max-players"));
                return false;
            }

            settings.setMaxPlayers(maxPlayers);
            player.sendMessage(MessageId.get("arena.setup.max-players-set", "value", String.valueOf(maxPlayers)));
            return true;
        } catch (NumberFormatException e) {
            player.sendMessage(MessageId.get("arena.setup.max-players-invalid"));
            player.sendMessage(MessageId.get("arena.setup.max-players"));
            return false;
        }
    }

    private boolean handleZoneDropItems(Player player, String message, ZoneSettings settings) {
        if (message.equalsIgnoreCase("yes")) {
            settings.setDropItems(true);
            player.sendMessage(MessageId.get("zone.setup.drop-items-set", "value", "yes"));
            return true;
        } else if (message.equalsIgnoreCase("no")) {
            settings.setDropItems(false);
            player.sendMessage(MessageId.get("zone.setup.drop-items-set", "value", "no"));
            return true;
        } else {
            player.sendMessage(MessageId.get("zone.setup.drop-items-invalid"));
            player.sendMessage(MessageId.get("zone.setup.drop-items"));
            return false;
        }
    }

    private boolean handleZoneNotchApple(Player player, String message, ZoneSettings settings) {
        try {
            int delay = Integer.parseInt(message);
            if (delay < 0) {
                player.sendMessage(MessageId.get("zone.setup.notch-apple-invalid"));
                player.sendMessage(MessageId.get("zone.setup.notch-apple"));
                return false;
            }

            settings.setNotchAppleDelay(delay);
            player.sendMessage(MessageId.get("zone.setup.notch-apple-set", "value", String.valueOf(delay)));
            return true;
        } catch (NumberFormatException e) {
            player.sendMessage(MessageId.get("zone.setup.notch-apple-invalid"));
            player.sendMessage(MessageId.get("zone.setup.notch-apple"));
            return false;
        }
    }

    private boolean handleZoneEnderpearl(Player player, String message, ZoneSettings settings) {
        try {
            int delay = Integer.parseInt(message);
            if (delay < 0) {
                player.sendMessage(MessageId.get("zone.setup.enderpearl-invalid"));
                player.sendMessage(MessageId.get("zone.setup.enderpearl"));
                return false;
            }

            settings.setEnderpearlDelay(delay);
            player.sendMessage(MessageId.get("zone.setup.enderpearl-set", "value", String.valueOf(delay)));
            return true;
        } catch (NumberFormatException e) {
            player.sendMessage(MessageId.get("zone.setup.enderpearl-invalid"));
            player.sendMessage(MessageId.get("zone.setup.enderpearl"));
            return false;
        }
    }
}
package com.copestudios.csspvp.listeners;

import com.copestudios.csspvp.CSSPVP;
import com.copestudios.csspvp.arena.Arena;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

public class ArenaListener implements Listener {

    private final CSSPVP plugin;

    public ArenaListener(CSSPVP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        // Check if player is teleporting out of arena without using the leave command
        if (plugin.getArenaManager().isInArena(player)) {
            Arena arena = plugin.getArenaManager().getPlayerArena(player);

            if (!arena.isInside(event.getTo()) && !event.getTo().equals(arena.getSpawnLocation())) {
                // Only allow teleport if it's initiated by the plugin (e.g., leave command)
                if (event.getCause() != PlayerTeleportEvent.TeleportCause.PLUGIN) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();

        // Prevent hunger in lobby
        if (isInLobby(player) && !canBypassLobbyRestrictions(player)) {
            if (plugin.getConfigManager().getConfig().getBoolean("lobby.prevent-hunger", true)) {
                event.setCancelled(true);
            }
        }

        // Manage hunger in arena
        if (plugin.getArenaManager().isInArena(player)) {
            Arena arena = plugin.getArenaManager().getPlayerArena(player);
            if (plugin.getConfigManager().getConfig().getBoolean("arena.prevent-hunger", false)) {
                event.setCancelled(true);
            }
        }
    }

    private boolean isInLobby(Player player) {
        // Check if player is in lobby world
        return plugin.getConfigManager().getLobbyLocation() != null &&
                player.getWorld().equals(plugin.getConfigManager().getLobbyLocation().getWorld());
    }

    private boolean canBypassLobbyRestrictions(Player player) {
        return player.hasPermission("csspvp.bypass.lobby") &&
                plugin.getConfigManager().getConfig().getBoolean("lobby.op-bypass", true);
    }
}
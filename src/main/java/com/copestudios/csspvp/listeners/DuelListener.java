package com.copestudios.csspvp.listeners;

import com.copestudios.csspvp.CSSPVP;
import com.copestudios.csspvp.duel.Duel;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class DuelListener implements Listener {

    private final CSSPVP plugin;

    public DuelListener(CSSPVP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        // Check if player is teleporting out of duel zone without using the leave command
        if (plugin.getDuelManager().isInDuel(player)) {
            Duel duel = plugin.getDuelManager().getPlayerDuel(player);

            // Only allow teleport if it's initiated by the plugin (e.g., leave command or end of duel)
            if (event.getCause() != PlayerTeleportEvent.TeleportCause.PLUGIN) {
                if (duel.getState() == Duel.DuelState.ACTIVE || duel.getState() == Duel.DuelState.COUNTDOWN) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Only check if player has changed block position
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();

        // Check if player is in duel countdown
        if (plugin.getDuelManager().isInDuel(player)) {
            Duel duel = plugin.getDuelManager().getPlayerDuel(player);

            if (duel.getState() == Duel.DuelState.COUNTDOWN) {
                // Prevent movement during countdown
                event.setCancelled(true);
            }
        }
    }
}
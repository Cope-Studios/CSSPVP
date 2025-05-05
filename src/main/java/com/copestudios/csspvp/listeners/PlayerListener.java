package com.copestudios.csspvp.listeners;

import com.copestudios.csspvp.CSSPVP;
import com.copestudios.csspvp.arena.Arena;
import com.copestudios.csspvp.duel.Duel;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerListener implements Listener {

    private final CSSPVP plugin;

    public PlayerListener(CSSPVP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Teleport to lobby if set
        Location lobby = plugin.getConfigManager().getLobbyLocation();
        if (lobby != null) {
            player.teleport(lobby);
        }

        // Clear inventory if configured to do so
        if (plugin.getConfigManager().getConfig().getBoolean("lobby.clear-inventory-on-join", true)) {
            player.getInventory().clear();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Check if player is in arena
        if (plugin.getArenaManager().isInArena(player)) {
            plugin.getArenaManager().leaveArena(player);
        }

        // Check if player is in duel
        if (plugin.getDuelManager().isInDuel(player)) {
            Duel duel = plugin.getDuelManager().getPlayerDuel(player);
            plugin.getDuelManager().forfeitDuel(player);
        }

        // Check if player is in random duel
        if (plugin.getRandomDuelManager().isInRandomDuel(player)) {
            plugin.getRandomDuelManager().forfeitRandomDuel(player);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        // Check if player is in arena
        if (plugin.getArenaManager().isInArena(player)) {
            // Prevent item drops if configured
            Arena arena = plugin.getArenaManager().getPlayerArena(player);
            if (!arena.getSettings().canDropItems()) {
                event.getDrops().clear();
            }

            // Reduce lives
            plugin.getArenaManager().reducePlayerLife(player);

            // Schedule respawn at arena spawn
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isOnline()) {
                        player.spigot().respawn();
                    }
                }
            }.runTaskLater(plugin, 1L);
        }

        // Check if player is in duel
        if (plugin.getDuelManager().isInDuel(player)) {
            Duel duel = plugin.getDuelManager().getPlayerDuel(player);

            // Prevent item drops
            event.getDrops().clear();

            // End duel with opponent as winner
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (duel.getState() != Duel.DuelState.FINISHED) {
                        plugin.getDuelManager().endDuel(duel, duel.getOpponent(player.getUniqueId()));
                    }
                }
            }.runTaskLater(plugin, 1L);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        // Check if player is in arena
        if (plugin.getArenaManager().isInArena(player)) {
            Arena arena = plugin.getArenaManager().getPlayerArena(player);

            // Set respawn location to arena spawn
            event.setRespawnLocation(arena.getSpawnLocation());

            // Give arena kit after respawn
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isOnline()) {
                        int delay = arena.getSettings().getKitDelay();
                        plugin.getKitManager().giveKitWithDelay(player, arena.getName(), delay);
                    }
                }
            }.runTaskLater(plugin, 5L);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        // Check if in the lobby area
        if (isInLobby(player) && !canBypassLobbyRestrictions(player)) {
            if (plugin.getConfigManager().getConfig().getBoolean("lobby.prevent-block-break", true)) {
                event.setCancelled(true);
            }
        }

        // Check if in an arena
        if (plugin.getArenaManager().isInArena(player)) {
            Arena arena = plugin.getArenaManager().getPlayerArena(player);
            event.setCancelled(true);
        }

        // Check if in a duel
        if (plugin.getDuelManager().isInDuel(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        // Check if in the lobby area
        if (isInLobby(player) && !canBypassLobbyRestrictions(player)) {
            if (plugin.getConfigManager().getConfig().getBoolean("lobby.prevent-block-place", true)) {
                event.setCancelled(true);
            }
        }

        // Check if in an arena
        if (plugin.getArenaManager().isInArena(player)) {
            Arena arena = plugin.getArenaManager().getPlayerArena(player);
            event.setCancelled(true);
        }

        // Check if in a duel
        if (plugin.getDuelManager().isInDuel(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();

        // Check if in the lobby area
        if (isInLobby(player) && !canBypassLobbyRestrictions(player)) {
            if (plugin.getConfigManager().getConfig().getBoolean("lobby.prevent-damage", true)) {
                event.setCancelled(true);
            }
        }

        // Check if in a duel countdown
        if (plugin.getDuelManager().isInDuel(player)) {
            Duel duel = plugin.getDuelManager().getPlayerDuel(player);
            if (duel.getState() == Duel.DuelState.COUNTDOWN) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        Player damager = (Player) event.getDamager();

        // Check if in the lobby area
        if ((isInLobby(player) || isInLobby(damager)) &&
                (!canBypassLobbyRestrictions(player) || !canBypassLobbyRestrictions(damager))) {
            if (plugin.getConfigManager().getConfig().getBoolean("lobby.prevent-player-damage", true)) {
                event.setCancelled(true);
            }
        }

        // Check if in same arena
        if (plugin.getArenaManager().isInArena(player) && plugin.getArenaManager().isInArena(damager)) {
            Arena playerArena = plugin.getArenaManager().getPlayerArena(player);
            Arena damagerArena = plugin.getArenaManager().getPlayerArena(damager);

            if (!playerArena.getName().equals(damagerArena.getName())) {
                event.setCancelled(true);
            }
        }

        // Check if in a duel
        if (plugin.getDuelManager().isInDuel(player) && plugin.getDuelManager().isInDuel(damager)) {
            Duel playerDuel = plugin.getDuelManager().getPlayerDuel(player);
            Duel damagerDuel = plugin.getDuelManager().getPlayerDuel(damager);

            if (playerDuel != damagerDuel) {
                event.setCancelled(true);
            } else if (playerDuel.getState() != Duel.DuelState.ACTIVE) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        // Check if in arena
        if (plugin.getArenaManager().isInArena(player)) {
            Arena arena = plugin.getArenaManager().getPlayerArena(player);
            if (!arena.getSettings().canDropItems()) {
                event.setCancelled(true);
            }
        }

        // Check if in duel
        if (plugin.getDuelManager().isInDuel(player)) {
            Duel duel = plugin.getDuelManager().getPlayerDuel(player);
            if (!duel.getZone().getSettings().canDropItems()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();

        // Check if in arena
        if (plugin.getArenaManager().isInArena(player)) {
            Arena arena = plugin.getArenaManager().getPlayerArena(player);
            if (!arena.getSettings().canDropItems()) {
                event.setCancelled(true);
            }
        }

        // Check if in duel
        if (plugin.getDuelManager().isInDuel(player)) {
            Duel duel = plugin.getDuelManager().getPlayerDuel(player);
            if (!duel.getZone().getSettings().canDropItems()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Check for notch apple in arena
        if (plugin.getArenaManager().isInArena(player)) {
            if (item.getType().name().contains("GOLDEN_APPLE") && item.getType().name().contains("ENCHANTED")) {
                Arena arena = plugin.getArenaManager().getPlayerArena(player);
                int delay = arena.getSettings().getNotchAppleDelay();

                if (delay > 0) {
                    // TODO: Implement cooldown system for items
                }
            }
        }

        // Check for notch apple in duel
        if (plugin.getDuelManager().isInDuel(player)) {
            if (item.getType().name().contains("GOLDEN_APPLE") && item.getType().name().contains("ENCHANTED")) {
                Duel duel = plugin.getDuelManager().getPlayerDuel(player);
                int delay = duel.getZone().getSettings().getNotchAppleDelay();

                if (delay > 0) {
                    // TODO: Implement cooldown system for items
                }
            }
        }
    }

    private boolean isInLobby(Player player) {
        // Check if player is in lobby world
        Location lobby = plugin.getConfigManager().getLobbyLocation();
        return lobby != null && player.getWorld().equals(lobby.getWorld());
    }

    private boolean canBypassLobbyRestrictions(Player player) {
        return player.hasPermission("csspvp.bypass.lobby") &&
                plugin.getConfigManager().getConfig().getBoolean("lobby.op-bypass", true);
    }
}
package com.copestudios.csspvp.listeners;

import com.copestudios.csspvp.CSSPVP;
import com.copestudios.csspvp.arena.Arena;
import com.copestudios.csspvp.arena.ArenaManager;
import com.copestudios.csspvp.messages.MessageManager;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerListener implements Listener {
    private final CSSPVP plugin;

    public PlayerListener(CSSPVP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Teleport to general spawn if enabled
        if (plugin.getConfigManager().getBoolean("general.spawn-enabled", true)) {
            Location spawnLocation = plugin.getConfigManager().getGeneralSpawn();
            if (spawnLocation != null) {
                event.getPlayer().teleport(spawnLocation);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Remove from arena if in one
        Arena arena = plugin.getArenaManager().getPlayerArena(player);
        if (arena != null) {
            arena.removePlayer(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // Only handle player-to-player damage
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) {
            return;
        }

        Player victim = (Player) event.getEntity();
        Player attacker = (Player) event.getDamager();

        // Check if in same arena
        Arena victimArena = plugin.getArenaManager().getPlayerArena(victim);
        Arena attackerArena = plugin.getArenaManager().getPlayerArena(attacker);

        // If both in same arena and arena is active, allow PVP
        if (victimArena != null && attackerArena != null
                && victimArena.equals(attackerArena)
                && victimArena.getState() == Arena.ArenaState.ACTIVE) {

            // If team-based, check if they're on the same team
            if (victimArena.getTeamSize() > 1) {
                for (com.copestudios.csspvp.team.Team team : victimArena.getTeams()) {
                    if (team.isMember(victim.getUniqueId()) && team.isMember(attacker.getUniqueId())) {
                        // Same team, cancel damage
                        event.setCancelled(true);
                        return;
                    }
                }
            }

            // Different teams or solo mode, allow damage
            return;
        }

        // Check if in any arena
        boolean victimInArena = plugin.getArenaManager().isLocationInAnyArena(victim.getLocation());
        boolean attackerInArena = plugin.getArenaManager().isLocationInAnyArena(attacker.getLocation());

        if (victimInArena && attackerInArena) {
            // Both in arena locations but not properly in an arena (could be spectating)
            event.setCancelled(true);
            return;
        }

        // If PVP outside arenas is disabled, cancel damage
        if (!plugin.getConfigManager().isPvpOutsideArenaAllowed()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Player killer = player.getKiller();

        // Check if player is in an arena
        Arena arena = plugin.getArenaManager().getPlayerArena(player);
        if (arena != null) {
            // Prevent drops in arena
            event.getDrops().clear();
            event.setDroppedExp(0);

            // Handle arena death
            arena.onPlayerDeath(player, killer);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        // If player was in an arena, respawn at arena spawn point or general spawn
        Arena arena = plugin.getArenaManager().getPlayerArena(player);
        if (arena != null && arena.getSpawnPoint() != null) {
            event.setRespawnLocation(arena.getSpawnPoint());
        } else if (plugin.getConfigManager().getGeneralSpawn() != null) {
            event.setRespawnLocation(plugin.getConfigManager().getGeneralSpawn());
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        // If player is op or in creative, allow block breaking
        if (player.isOp() || player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        // Check if block breaking is globally disabled
        if (!plugin.getConfigManager().isBlockBreakAllowed()) {
            event.setCancelled(true);
            return;
        }

        // Check if the block is in an arena
        Arena arena = plugin.getArenaManager().getArenaAtLocation(event.getBlock().getLocation());

        // Only allow breaking in active arenas the player is in
        if (arena == null || arena.getState() != Arena.ArenaState.ACTIVE ||
                !arena.getParticipants().containsKey(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        // If player is op or in creative, allow block placing
        if (player.isOp() || player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        // Check if block placing is globally disabled
        if (!plugin.getConfigManager().isBlockPlaceAllowed()) {
            event.setCancelled(true);
            return;
        }

        // Check if the block is in an arena
        Arena arena = plugin.getArenaManager().getArenaAtLocation(event.getBlock().getLocation());

        // Only allow placing in active arenas the player is in
        if (arena == null || arena.getState() != Arena.ArenaState.ACTIVE ||
                !arena.getParticipants().containsKey(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        // If player is op or in creative, allow item dropping
        if (player.isOp() || player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        // Check if item dropping is globally disabled
        if (!plugin.getConfigManager().isItemDropAllowed()) {
            event.setCancelled(true);
            return;
        }

        // Check if player is in an arena
        Arena arena = plugin.getArenaManager().getPlayerArena(player);

        // Only allow dropping in active arenas
        if (arena == null || arena.getState() != Arena.ArenaState.ACTIVE) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        // Only handle players
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();

        // Check if hunger is disabled
        if (!plugin.getConfigManager().isHungerEnabled()) {
            // Only cancel hunger loss (not gain)
            if (event.getFoodLevel() < player.getFoodLevel()) {
                event.setCancelled(true);
                return;
            }
        }

        // Check if player is in an active arena
        Arena arena = plugin.getArenaManager().getPlayerArena(player);
        if (arena != null && arena.getState() == Arena.ArenaState.ACTIVE) {
            // Allow hunger in active arenas
            return;
        }

        // Prevent hunger loss outside arenas
        if (event.getFoodLevel() < player.getFoodLevel()) {
            event.setCancelled(true);
        }
    }
}
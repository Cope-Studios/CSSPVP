package com.copestudios.csspvp.gui;

import com.copestudios.csspvp.CSSPVP;
import com.copestudios.csspvp.arena.Arena;
import com.copestudios.csspvp.duel.DuelManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GuiListener implements Listener {
    private final CSSPVP plugin;
    private final Map<UUID, UUID> selectedDuelTargets = new HashMap<>();
    private final DuelManager duelManager;

    public GuiListener(CSSPVP plugin) {
        this.plugin = plugin;
        this.duelManager = new DuelManager(plugin);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        ItemStack clickedItem = event.getCurrentItem();

        // Prevent taking items
        event.setCancelled(true);

        if (clickedItem == null || clickedItem.getItemMeta() == null) return;

        // Handle different GUI menus
        if (title.equals("§cCSS PVP - Main Menu")) {
            handleMainMenuClick(player, clickedItem, event.getSlot());
        } else if (title.equals("§cCSS PVP - Arenas")) {
            handleArenaMenuClick(player, clickedItem, event.getSlot(), event.isRightClick());
        } else if (title.equals("§cCSS PVP - Teams")) {
            handleTeamMenuClick(player, clickedItem, event.getSlot(), event.isRightClick());
        } else if (title.equals("§cCSS PVP - Duels")) {
            handleDuelMenuClick(player, clickedItem, event.getSlot());
        } else if (title.equals("§cCSS PVP - Select Arena for Duel")) {
            handleDuelArenaMenuClick(player, clickedItem, event.getSlot());
        }
    }

    private void handleMainMenuClick(Player player, ItemStack clickedItem, int slot) {
        String name = clickedItem.getItemMeta().getDisplayName();

        if (name.equals("§cArenas")) {
            plugin.getGuiManager().openArenaMenu(player);
        } else if (name.equals("§cTeams")) {
            plugin.getGuiManager().openTeamMenu(player);
        } else if (name.equals("§cDuels")) {
            plugin.getGuiManager().openDuelMenu(player);
        }
    }

    private void handleArenaMenuClick(Player player, ItemStack clickedItem, int slot, boolean isRightClick) {
        String name = clickedItem.getItemMeta().getDisplayName();

        if (name.equals("§cBack")) {
            plugin.getGuiManager().openMainMenu(player);
            return;
        }

        if (name.equals("§aCreate Arena")) {
            // Close inventory and prompt for arena name
            player.closeInventory();
            player.sendMessage(plugin.colorize("&aEnter arena name in chat:"));
            // Here you'd normally register a chat listener for the player
            // For simplicity, we'll just ask them to use the command
            player.sendMessage(plugin.colorize("&7Use: /cssarena create <name>"));
            return;
        }

        // Handle arena slots
        if (name.startsWith("§b")) {
            String arenaName = name.substring(2); // Remove §b prefix
            Arena arena = plugin.getArenaManager().getArena(arenaName);

            if (arena == null) return;

            if (isRightClick && player.hasPermission("csspvp.arena.create")) {
                // Open arena options menu
                player.closeInventory();
                player.performCommand("cssarena info " + arenaName);
            } else if (player.hasPermission("csspvp.arena.join") && arena.isSetup()) {
                // Join arena
                player.closeInventory();
                player.performCommand("cssarena join " + arenaName);
            }
        }
    }

    private void handleTeamMenuClick(Player player, ItemStack clickedItem, int slot, boolean isRightClick) {
        String name = clickedItem.getItemMeta().getDisplayName();

        if (name.equals("§cBack")) {
            plugin.getGuiManager().openMainMenu(player);
            return;
        }

        if (name.equals("§aCreate Team")) {
            // Close inventory and prompt for team name
            player.closeInventory();
            player.sendMessage(plugin.colorize("&aEnter team name in chat:"));
            // Here you'd normally register a chat listener for the player
            // For simplicity, we'll just ask them to use the command
            player.sendMessage(plugin.colorize("&7Use: /cssteam create <name>"));
            return;
        }

        // Handle "Your Team" slot
        if (name.startsWith("§aYour Team: ")) {
            player.closeInventory();
            player.performCommand("cssteam leave");
            return;
        }

        // Handle team slots
        if (name.startsWith("§b")) {
            String teamName = name.substring(2); // Remove §b prefix

            if (isRightClick && player.hasPermission("csspvp.admin")) {
                // Open team options menu
                player.closeInventory();
                player.performCommand("cssteam info " + teamName);
            } else if (player.hasPermission("csspvp.team")) {
                // Join team
                player.closeInventory();
                player.performCommand("cssteam join " + teamName);
            }
        }
    }

    private void handleDuelMenuClick(Player player, ItemStack clickedItem, int slot) {
        String name = clickedItem.getItemMeta().getDisplayName();

        if (name.equals("§cBack")) {
            plugin.getGuiManager().openMainMenu(player);
            return;
        }

        if (name.equals("§aRandom Duel") && player.hasPermission("csspvp.admin")) {
            // Close inventory and prompt for arena name
            player.closeInventory();
            player.sendMessage(plugin.colorize("&aEnter arena name for random duel:"));
            player.sendMessage(plugin.colorize("&7Use: /cssduel random <arena>"));
            return;
        }

        // Handle player slots for dueling
        if (name.startsWith("§b")) {
            String targetName = name.substring(2); // Remove §b prefix
            Player target = Bukkit.getPlayer(targetName);

            if (target != null && target.isOnline() && player.hasPermission("csspvp.duel")) {
                // Store selected target and open arena selection
                selectedDuelTargets.put(player.getUniqueId(), target.getUniqueId());
                plugin.getGuiManager().openDuelArenaMenu(player, target);
            }
        }
    }

    private void handleDuelArenaMenuClick(Player player, ItemStack clickedItem, int slot) {
        String name = clickedItem.getItemMeta().getDisplayName();

        if (name.equals("§cBack")) {
            plugin.getGuiManager().openDuelMenu(player);
            return;
        }

        // Handle arena slots for dueling
        if (name.startsWith("§b")) {
            String arenaName = name.substring(2); // Remove §b prefix

            // Get stored target
            UUID targetUUID = selectedDuelTargets.get(player.getUniqueId());
            if (targetUUID == null) return;

            Player target = Bukkit.getPlayer(targetUUID);
            if (target == null || !target.isOnline()) return;

            // Send duel request
            player.closeInventory();
            player.performCommand("cssduel challenge " + target.getName() + " " + arenaName);

            // Remove from map
            selectedDuelTargets.remove(player.getUniqueId());
        }
    }
}
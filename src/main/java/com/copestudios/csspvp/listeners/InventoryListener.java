package com.copestudios.csspvp.listeners;

import com.copestudios.csspvp.CSSPVP;
import com.copestudios.csspvp.gui.ArenaSettingsGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;

public class InventoryListener implements Listener {

    private final CSSPVP plugin;

    public InventoryListener(CSSPVP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        // Handle custom GUI clicks
        if (holder instanceof ArenaSettingsGUI) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null) {
                return;
            }

            ArenaSettingsGUI gui = (ArenaSettingsGUI) holder;
            gui.handleClick((Player) event.getWhoClicked(), event.getSlot(), event.getClick());
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        // Handle custom GUI closing
        if (holder instanceof ArenaSettingsGUI) {
            ArenaSettingsGUI gui = (ArenaSettingsGUI) holder;
            gui.handleClose((Player) event.getPlayer());
        }
    }
}
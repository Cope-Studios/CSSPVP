package com.copestudios.csspvp.gui;

import com.copestudios.csspvp.CSSPVP;
import com.copestudios.csspvp.arena.Arena;
import com.copestudios.csspvp.arena.ArenaSettings;
import com.copestudios.csspvp.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ArenaSettingsGUI implements InventoryHolder {

    private final CSSPVP plugin;
    private final Player player;
    private final Arena arena;
    private final Inventory inventory;

    // GUI slot constants
    private static final int LIVES_SLOT = 10;
    private static final int DROP_ITEMS_SLOT = 12;
    private static final int NOTCH_APPLE_SLOT = 14;
    private static final int ENDERPEARL_SLOT = 16;
    private static final int MAX_PLAYERS_SLOT = 28;
    private static final int KIT_DELAY_SLOT = 30;
    private static final int EDIT_KIT_SLOT = 32;
    private static final int ADD_EFFECTS_SLOT = 34;

    public ArenaSettingsGUI(CSSPVP plugin, Player player, Arena arena) {
        this.plugin = plugin;
        this.player = player;
        this.arena = arena;
        this.inventory = Bukkit.createInventory(this, 45, ColorUtils.colorize("&8Arena Settings: &b" + arena.getName()));

        initializeItems();
    }

    private void initializeItems() {
        ArenaSettings settings = arena.getSettings();

        // Lives setting
        ItemStack livesItem = createItem(Material.HEART_OF_THE_SEA,
                "&aLives: &b" + (settings.hasInfiniteLives() ? "Infinite" : settings.getLives()),
                "&7Click to change the number of lives",
                "&7Left-click to increase",
                "&7Right-click to decrease",
                "&7Shift-click to toggle infinite");
        inventory.setItem(LIVES_SLOT, livesItem);

        // Drop items setting
        ItemStack dropItemsItem = createItem(settings.canDropItems() ? Material.HOPPER : Material.BARRIER,
                "&aDrop Items: &b" + (settings.canDropItems() ? "Yes" : "No"),
                "&7Click to toggle whether players",
                "&7can drop and pick up items");
        inventory.setItem(DROP_ITEMS_SLOT, dropItemsItem);

        // Notch apple delay setting
        ItemStack notchAppleItem = createItem(Material.ENCHANTED_GOLDEN_APPLE,
                "&aNotch Apple Delay: &b" + settings.getNotchAppleDelay() + "s",
                "&7Click to change the delay between",
                "&7eating Notch Apples (in seconds)",
                "&7Left-click to increase",
                "&7Right-click to decrease",
                "&7Shift-click to set to 0 (no delay)");
        inventory.setItem(NOTCH_APPLE_SLOT, notchAppleItem);

        // Enderpearl delay setting
        ItemStack enderpearlItem = createItem(Material.ENDER_PEARL,
                "&aEnderpearl Delay: &b" + settings.getEnderpearlDelay() + "s",
                "&7Click to change the delay between",
                "&7using Enderpearls (in seconds)",
                "&7Left-click to increase",
                "&7Right-click to decrease",
                "&7Shift-click to set to 0 (no delay)");
        inventory.setItem(ENDERPEARL_SLOT, enderpearlItem);

        // Max players setting
        ItemStack maxPlayersItem = createItem(Material.PLAYER_HEAD,
                "&aMax Players: &b" + (settings.getMaxPlayers() == -1 ? "Unlimited" : settings.getMaxPlayers()),
                "&7Click to change the maximum number",
                "&7of players that can join the arena",
                "&7Left-click to increase",
                "&7Right-click to decrease",
                "&7Shift-click to toggle unlimited");
        inventory.setItem(MAX_PLAYERS_SLOT, maxPlayersItem);

        // Kit delay setting
        ItemStack kitDelayItem = createItem(Material.CLOCK,
                "&aKit Delay: &b" + settings.getKitDelay() + "s",
                "&7Click to change the delay before",
                "&7giving players their kit (in seconds)",
                "&7Left-click to increase",
                "&7Right-click to decrease",
                "&7Shift-click to set to 0 (no delay)");
        inventory.setItem(KIT_DELAY_SLOT, kitDelayItem);

        // Edit kit button
        ItemStack editKitItem = createItem(Material.DIAMOND_CHESTPLATE,
                "&aEdit Kit",
                "&7Click to edit the arena kit");
        inventory.setItem(EDIT_KIT_SLOT, editKitItem);

        // Add effects button
        ItemStack addEffectsItem = createItem(Material.POTION,
                "&aAdd Effects",
                "&7Click to add potion effects",
                "&7to players in this arena");
        inventory.setItem(ADD_EFFECTS_SLOT, addEffectsItem);

        // Fill empty slots with gray glass pane
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ColorUtils.colorize(name));

        List<String> loreList = new ArrayList<>();
        for (String line : lore) {
            loreList.add(ColorUtils.colorize(line));
        }

        meta.setLore(loreList);
        item.setItemMeta(meta);

        return item;
    }

    public void open() {
        player.openInventory(inventory);
    }

    public void handleClick(Player player, int slot, ClickType clickType) {
        ArenaSettings settings = arena.getSettings();

        switch (slot) {
            case LIVES_SLOT:
                handleLivesClick(settings, clickType);
                break;
            case DROP_ITEMS_SLOT:
                settings.setDropItems(!settings.canDropItems());
                break;
            case NOTCH_APPLE_SLOT:
                handleNotchAppleClick(settings, clickType);
                break;
            case ENDERPEARL_SLOT:
                handleEnderpearlClick(settings, clickType);
                break;
            case MAX_PLAYERS_SLOT:
                handleMaxPlayersClick(settings, clickType);
                break;
            case KIT_DELAY_SLOT:
                handleKitDelayClick(settings, clickType);
                break;
            case EDIT_KIT_SLOT:
                // Close inventory and set player's inventory as the kit
                player.closeInventory();
                // Give the current kit to the player
                ItemStack[] kit = plugin.getKitManager().getArenaKit(arena.getName());
                if (kit != null) {
                    player.getInventory().setContents(kit);
                }
                player.sendMessage(ColorUtils.colorize("&aEdit the kit and use &b/csa setkit " + arena.getName() + " &ato save it."));
                return;
            case ADD_EFFECTS_SLOT:
                // TODO: Open effects GUI
                player.sendMessage(ColorUtils.colorize("&cEffects feature not implemented yet."));
                return;
        }

        // Save settings
        plugin.getArenaManager().saveArenas();

        // Update inventory
        initializeItems();
    }

    private void handleLivesClick(ArenaSettings settings, ClickType clickType) {
        if (clickType.isShiftClick()) {
            settings.setInfiniteLives(!settings.hasInfiniteLives());
        } else if (clickType.isLeftClick()) {
            if (!settings.hasInfiniteLives() && settings.getLives() < 10) {
                settings.setLives(settings.getLives() + 1);
            }
        } else if (clickType.isRightClick()) {
            if (!settings.hasInfiniteLives() && settings.getLives() > 1) {
                settings.setLives(settings.getLives() - 1);
            }
        }
    }

    private void handleNotchAppleClick(ArenaSettings settings, ClickType clickType) {
        if (clickType.isShiftClick()) {
            settings.setNotchAppleDelay(0);
        } else if (clickType.isLeftClick()) {
            settings.setNotchAppleDelay(settings.getNotchAppleDelay() + 5);
        } else if (clickType.isRightClick()) {
            if (settings.getNotchAppleDelay() >= 5) {
                settings.setNotchAppleDelay(settings.getNotchAppleDelay() - 5);
            } else if (settings.getNotchAppleDelay() > 0) {
                settings.setNotchAppleDelay(0);
            }
        }
    }

    private void handleEnderpearlClick(ArenaSettings settings, ClickType clickType) {
        if (clickType.isShiftClick()) {
            settings.setEnderpearlDelay(0);
        } else if (clickType.isLeftClick()) {
            settings.setEnderpearlDelay(settings.getEnderpearlDelay() + 5);
        } else if (clickType.isRightClick()) {
            if (settings.getEnderpearlDelay() >= 5) {
                settings.setEnderpearlDelay(settings.getEnderpearlDelay() - 5);
            } else if (settings.getEnderpearlDelay() > 0) {
                settings.setEnderpearlDelay(0);
            }
        }
    }

    private void handleMaxPlayersClick(ArenaSettings settings, ClickType clickType) {
        if (clickType.isShiftClick()) {
            if (settings.getMaxPlayers() == -1) {
                settings.setMaxPlayers(10);
            } else {
                settings.setMaxPlayers(-1);
            }
        } else if (clickType.isLeftClick()) {
            if (settings.getMaxPlayers() != -1 && settings.getMaxPlayers() < 100) {
                settings.setMaxPlayers(settings.getMaxPlayers() + 5);
            }
        } else if (clickType.isRightClick()) {
            if (settings.getMaxPlayers() != -1 && settings.getMaxPlayers() > 5) {
                settings.setMaxPlayers(settings.getMaxPlayers() - 5);
            } else if (settings.getMaxPlayers() != -1 && settings.getMaxPlayers() > 1) {
                settings.setMaxPlayers(1);
            }
        }
    }

    private void handleKitDelayClick(ArenaSettings settings, ClickType clickType) {
        if (clickType.isShiftClick()) {
            settings.setKitDelay(0);
        } else if (clickType.isLeftClick()) {
            settings.setKitDelay(settings.getKitDelay() + 1);
        } else if (clickType.isRightClick()) {
            if (settings.getKitDelay() > 0) {
                settings.setKitDelay(settings.getKitDelay() - 1);
            }
        }
    }

    public void handleClose(Player player) {
        // Nothing special to do on close
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
package com.copestudios.csspvp.gui;

import com.copestudios.csspvp.CSSPVP;
import com.copestudios.csspvp.arena.Arena;
import com.copestudios.csspvp.team.Team;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GuiManager {
    private final CSSPVP plugin;

    public GuiManager(CSSPVP plugin) {
        this.plugin = plugin;
    }

    // GUI identifiers
    private static final String MAIN_MENU_TITLE = "§cCSS PVP - Main Menu";
    private static final String ARENA_MENU_TITLE = "§cCSS PVP - Arenas";
    private static final String TEAM_MENU_TITLE = "§cCSS PVP - Teams";
    private static final String DUEL_MENU_TITLE = "§cCSS PVP - Duels";

    /**
     * Open the main menu for a player
     */
    public void openMainMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 27, MAIN_MENU_TITLE);

        // Add border
        addBorder(menu);

        // Add menu items
        menu.setItem(10, createMenuItem(Material.DIAMOND_SWORD, "§cArenas",
                "§7Click to manage arenas."));

        menu.setItem(13, createMenuItem(Material.SHIELD, "§cTeams",
                "§7Click to manage teams."));

        menu.setItem(16, createMenuItem(Material.GOLDEN_SWORD, "§cDuels",
                "§7Click to start duels."));

        player.openInventory(menu);
    }

    /**
     * Open the arena menu for a player
     */
    public void openArenaMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 54, ARENA_MENU_TITLE);

        // Add border
        addBorder(menu);

        // Add back button
        menu.setItem(49, createMenuItem(Material.BARRIER, "§cBack",
                "§7Go back to main menu."));

        // Add arena management buttons if player has permission
        if (player.hasPermission("csspvp.arena.create")) {
            menu.setItem(4, createMenuItem(Material.EMERALD_BLOCK, "§aCreate Arena",
                    "§7Click to create a new arena."));
        }

        // Add all arenas
        int slot = 10;

        for (Arena arena : plugin.getArenaManager().getAllArenas()) {
            String status;
            switch (arena.getState()) {
                case ACTIVE:
                    status = "§a[ACTIVE]";
                    break;
                case WAITING:
                    status = "§e[WAITING]";
                    break;
                case ENDING:
                    status = "§c[ENDING]";
                    break;
                default:
                    status = "§7[INACTIVE]";
                    break;
            }

            boolean isSetup = arena.isSetup();
            String setupStatus = isSetup ? "§aReady" : "§cNot Ready";

            List<String> lore = new ArrayList<>();
            lore.add("§7Status: " + status);
            lore.add("§7Setup: " + setupStatus);
            lore.add("§7Lives: §f" + arena.getMaxLives());
            lore.add("§7Team Size: §f" + arena.getTeamSize());
            lore.add("§7Players: §f" + arena.getParticipants().size());

            if (player.hasPermission("csspvp.arena.join") && isSetup) {
                lore.add("");
                lore.add("§aClick to join arena");
            }

            if (player.hasPermission("csspvp.arena.create")) {
                lore.add("");
                lore.add("§eRight-click for options");
            }

            Material material = isSetup ? Material.DIAMOND_SWORD : Material.IRON_SWORD;

            menu.setItem(slot, createMenuItem(material, "§b" + arena.getName(), lore));

            // Move to next slot
            slot++;
            if ((slot % 9) == 8) {
                slot += 2;
            }

            // Maximum of 28 arenas displayed
            if (slot > 43) {
                break;
            }
        }

        player.openInventory(menu);
    }

    /**
     * Open the team menu for a player
     */
    public void openTeamMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 54, TEAM_MENU_TITLE);

        // Add border
        addBorder(menu);

        // Add back button
        menu.setItem(49, createMenuItem(Material.BARRIER, "§cBack",
                "§7Go back to main menu."));

        // Add team management buttons if player has permission
        if (player.hasPermission("csspvp.team")) {
            menu.setItem(4, createMenuItem(Material.EMERALD_BLOCK, "§aCreate Team",
                    "§7Click to create a new team."));
        }

        // Add player's current team info if they're in a team
        Team playerTeam = plugin.getTeamManager().getPlayerTeam(player);
        if (playerTeam != null) {
            List<String> lore = new ArrayList<>();
            lore.add("§7Members: §f" + playerTeam.getSize());

            for (Player member : playerTeam.getOnlinePlayers()) {
                lore.add("§7- §f" + member.getName());
            }

            lore.add("");
            lore.add("§cClick to leave team");

            menu.setItem(8, createMenuItem(Material.SHIELD, "§aYour Team: §f" + playerTeam.getName(), lore));
        }

        // Add all teams
        int slot = 10;

        for (Team team : plugin.getTeamManager().getAllTeams()) {
            // Skip player's current team as it's already displayed
            if (playerTeam != null && team.getName().equals(playerTeam.getName())) {
                continue;
            }

            List<String> lore = new ArrayList<>();
            lore.add("§7Members: §f" + team.getSize());

            for (Player member : team.getOnlinePlayers()) {
                lore.add("§7- §f" + member.getName());
            }

            if (player.hasPermission("csspvp.team")) {
                lore.add("");
                lore.add("§aClick to join team");
            }

            if (player.hasPermission("csspvp.admin")) {
                lore.add("");
                lore.add("§eRight-click for options");
            }

            menu.setItem(slot, createMenuItem(Material.SHIELD, "§b" + team.getName(), lore));

            // Move to next slot
            slot++;
            if ((slot % 9) == 8) {
                slot += 2;
            }

            // Maximum of 28 teams displayed
            if (slot > 43) {
                break;
            }
        }

        player.openInventory(menu);
    }

    /**
     * Open the duel menu for a player
     */
    public void openDuelMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 54, DUEL_MENU_TITLE);

        // Add border
        addBorder(menu);

        // Add back button
        menu.setItem(49, createMenuItem(Material.BARRIER, "§cBack",
                "§7Go back to main menu."));

        // Add random duel button if player has permission
        if (player.hasPermission("csspvp.admin")) {
            menu.setItem(4, createMenuItem(Material.NETHER_STAR, "§aRandom Duel",
                    "§7Start a random duel between players."));
        }

        // Add online players to challenge
        int slot = 10;

        for (Player target : Bukkit.getOnlinePlayers()) {
            // Skip self
            if (target.equals(player)) {
                continue;
            }

            // Skip players already in arenas
            if (plugin.getArenaManager().isPlayerInArena(target)) {
                continue;
            }

            List<String> lore = new ArrayList<>();

            // Add team info if target is in a team
            Team targetTeam = plugin.getTeamManager().getPlayerTeam(target);
            if (targetTeam != null) {
                lore.add("§7Team: §f" + targetTeam.getName());
            }

            if (player.hasPermission("csspvp.duel")) {
                lore.add("");
                lore.add("§aClick to select arenas for duel");
            }

            menu.setItem(slot, createSkullItem(target.getName(), "§b" + target.getName(), lore));

            // Move to next slot
            slot++;
            if ((slot % 9) == 8) {
                slot += 2;
            }

            // Maximum of 28 players displayed
            if (slot > 43) {
                break;
            }
        }

        player.openInventory(menu);
    }

    /**
     * Open a menu to select an arena for dueling with a specific player
     */
    public void openDuelArenaMenu(Player player, Player target) {
        Inventory menu = Bukkit.createInventory(null, 54, "§cCSS PVP - Select Arena for Duel");

        // Add border
        addBorder(menu);

        // Add back button
        menu.setItem(49, createMenuItem(Material.BARRIER, "§cBack",
                "§7Go back to duels menu."));

        // Add player info
        List<String> targetLore = new ArrayList<>();
        targetLore.add("§7Select an arena to challenge this player.");

        menu.setItem(4, createSkullItem(target.getName(), "§aDuel with: §f" + target.getName(),
                targetLore));

        // Add available arenas
        int slot = 10;

        for (Arena arena : plugin.getArenaManager().getAllArenas()) {
            // Only show inactive and fully setup arenas
            if (arena.getState() != Arena.ArenaState.INACTIVE || !arena.isSetup()) {
                continue;
            }

            List<String> lore = new ArrayList<>();
            lore.add("§7Lives: §f" + arena.getMaxLives());
            lore.add("§7Team Size: §f" + arena.getTeamSize());
            lore.add("");
            lore.add("§aClick to challenge to duel");

            menu.setItem(slot, createMenuItem(Material.DIAMOND_SWORD, "§b" + arena.getName(), lore));

            // Move to next slot
            slot++;
            if ((slot % 9) == 8) {
                slot += 2;
            }

            // Maximum of 28 arenas displayed
            if (slot > 43) {
                break;
            }
        }

        player.openInventory(menu);
    }

    /**
     * Add a border to an inventory
     */
    private void addBorder(Inventory inventory) {
        ItemStack border = createMenuItem(Material.BLACK_STAINED_GLASS_PANE, " ", "");

        // Top and bottom rows
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, border);
            inventory.setItem(inventory.getSize() - 9 + i, border);
        }

        // Side columns
        for (int i = 9; i < inventory.getSize() - 9; i += 9) {
            inventory.setItem(i, border);
            inventory.setItem(i + 8, border);
        }
    }

    /**
     * Create a menu item
     */
    private ItemStack createMenuItem(Material material, String name, String lore) {
        List<String> loreList = new ArrayList<>();
        if (!lore.isEmpty()) {
            loreList.add(lore);
        }
        return createMenuItem(material, name, loreList);
    }

    /**
     * Create a menu item with multiple lore lines
     */
    private ItemStack createMenuItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(name);

        if (!lore.isEmpty()) {
            meta.setLore(lore);
        }

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Create a player head item
     */
    private ItemStack createSkullItem(String owner, String name, List<String> lore) {
        // In 1.21.4, player heads should be created differently
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);

        // This is a simplification - in a real plugin you'd use SkullMeta properly
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);

        if (lore != null && !lore.isEmpty()) {
            meta.setLore(lore);
        }

        item.setItemMeta(meta);
        return item;
    }
}
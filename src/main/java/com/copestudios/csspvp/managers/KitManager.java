package com.copestudios.csspvp.managers;

import com.copestudios.csspvp.CSSPVP;
import com.copestudios.csspvp.utils.MessageId;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KitManager {

    private final CSSPVP plugin;
    private final Map<String, ItemStack[]> kits;
    private final Map<UUID, BukkitTask> kitDelayTasks;

    public KitManager(CSSPVP plugin) {
        this.plugin = plugin;
        this.kits = new HashMap<>();
        this.kitDelayTasks = new HashMap<>();

        // Load default kit if exists
        loadKit("default");
    }

    public void setArenaKit(String arenaName, ItemStack[] contents) {
        plugin.getArenaManager().setKit(arenaName, contents);
    }

    public ItemStack[] getArenaKit(String arenaName) {
        return plugin.getArenaManager().getKit(arenaName);
    }

    public void setKit(String kitName, ItemStack[] contents) {
        kits.put(kitName.toLowerCase(), contents);
        saveKit(kitName, contents);
    }

    public ItemStack[] getKit(String kitName) {
        String lowerName = kitName.toLowerCase();

        // First check arena kits
        ItemStack[] arenaKit = getArenaKit(lowerName);
        if (arenaKit != null) {
            return arenaKit;
        }

        // Then check global kits
        return kits.get(lowerName);
    }

    private void saveKit(String kitName, ItemStack[] contents) {
        plugin.getConfigManager().getConfig().set("kits." + kitName.toLowerCase(), contents);
        plugin.getConfigManager().saveConfig();
    }

    private void loadKit(String kitName) {
        ItemStack[] contents = (ItemStack[]) plugin.getConfigManager().getConfig().get("kits." + kitName.toLowerCase());
        if (contents != null) {
            kits.put(kitName.toLowerCase(), contents);
        }
    }

    public void giveKit(Player player, String kitName) {
        ItemStack[] kit = getKit(kitName);
        if (kit == null) {
            player.sendMessage(MessageId.get("kit.not-found", "name", kitName));
            return;
        }

        player.getInventory().setContents(kit);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("name", kitName);
        player.sendMessage(MessageId.get("kit.given", placeholders));
    }

    public void giveKitWithDelay(Player player, String kitName, int delaySeconds) {
        UUID playerId = player.getUniqueId();

        // Cancel any existing delay task
        if (kitDelayTasks.containsKey(playerId)) {
            kitDelayTasks.remove(playerId).cancel();
        }

        // Notify player about delay
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("name", kitName);
        placeholders.put("seconds", String.valueOf(delaySeconds));
        player.sendMessage(MessageId.get("kit.delay", placeholders));

        // Create delay task
        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            giveKit(player, kitName);
            kitDelayTasks.remove(playerId);
        }, delaySeconds * 20L);

        kitDelayTasks.put(playerId, task);
    }

    public void giveKitToAll(String arenaName) {
        ItemStack[] kit = getArenaKit(arenaName);
        if (kit == null) {
            return;
        }

        // Get delay
        int delay = 0;
        try {
            delay = plugin.getArenaManager().getArena(arenaName).getSettings().getKitDelay();
        } catch (Exception e) {
            // Arena not found or settings null, use default
            delay = 3;
        }

        // Give kit to all players in arena
        for (UUID playerId : plugin.getArenaManager().getArena(arenaName).getPlayers()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                giveKitWithDelay(player, arenaName, delay);
            }
        }
    }

    public void giveKitToPlayer(String kitName, String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            return;
        }

        giveKit(player, kitName);
    }

    public void cleanUp() {
        // Cancel all delay tasks
        for (BukkitTask task : kitDelayTasks.values()) {
            task.cancel();
        }
        kitDelayTasks.clear();
    }
}
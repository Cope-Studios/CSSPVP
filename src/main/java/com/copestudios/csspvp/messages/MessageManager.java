package com.copestudios.csspvp.messages;

import com.copestudios.csspvp.CSSPVP;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MessageManager {
    private final CSSPVP plugin;
    private FileConfiguration messages;
    private File messagesFile;

    // Message IDs
    public static final String GENERAL_PREFIX = "general.prefix";
    public static final String GENERAL_NO_PERMISSION = "general.no_permission";
    public static final String GENERAL_PLAYER_ONLY = "general.player_only";
    public static final String GENERAL_INVALID_ARGS = "general.invalid_args";
    public static final String GENERAL_PLAYER_NOT_FOUND = "general.player_not_found";

    public static final String ARENA_CREATED = "arena.created";
    public static final String ARENA_DELETED = "arena.deleted";
    public static final String ARENA_NOT_FOUND = "arena.not_found";
    public static final String ARENA_ALREADY_EXISTS = "arena.already_exists";
    public static final String ARENA_SPAWN_SET = "arena.spawn_set";
    public static final String ARENA_CORNER_SET = "arena.corner_set";
    public static final String ARENA_JOIN = "arena.join";
    public static final String ARENA_LEAVE = "arena.leave";
    public static final String ARENA_FULL = "arena.full";
    public static final String ARENA_START = "arena.start";
    public static final String ARENA_END = "arena.end";
    public static final String ARENA_WINNER = "arena.winner";
    public static final String ARENA_KILL = "arena.kill";
    public static final String ARENA_DEATH = "arena.death";
    public static final String ARENA_SPECTATOR = "arena.spectator";

    public static final String TEAM_CREATED = "team.created";
    public static final String TEAM_DELETED = "team.deleted";
    public static final String TEAM_NOT_FOUND = "team.not_found";
    public static final String TEAM_ALREADY_EXISTS = "team.already_exists";
    public static final String TEAM_JOIN = "team.join";
    public static final String TEAM_LEAVE = "team.leave";
    public static final String TEAM_FULL = "team.full";
    public static final String TEAM_PLAYER_ADDED = "team.player_added";
    public static final String TEAM_PLAYER_REMOVED = "team.player_removed";

    public static final String DUEL_SENT = "duel.sent";
    public static final String DUEL_RECEIVED = "duel.received";
    public static final String DUEL_ACCEPTED = "duel.accepted";
    public static final String DUEL_DECLINED = "duel.declined";
    public static final String DUEL_EXPIRED = "duel.expired";
    public static final String DUEL_ALREADY_SENT = "duel.already_sent";
    public static final String DUEL_RANDOM_SELECTED = "duel.random_selected";

    public static final String SPAWN_SET = "spawn.set";
    public static final String SPAWN_TELEPORT = "spawn.teleport";

    public MessageManager(CSSPVP plugin) {
        this.plugin = plugin;
    }

    public void loadMessages() {
        // Create default messages file if it doesn't exist
        if (!new File(plugin.getDataFolder(), "messages.yml").exists()) {
            plugin.saveResource("messages.yml", false);
        }

        // Load messages
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        messages = YamlConfiguration.loadConfiguration(messagesFile);

        // Set default messages if they don't exist
        setDefaultMessages();

        plugin.getLogger().info("Messages loaded successfully!");
    }

    private void setDefaultMessages() {
        // Get default messages from resources
        InputStreamReader inputStream = new InputStreamReader(
                plugin.getResource("messages.yml"),
                StandardCharsets.UTF_8
        );

        if (inputStream != null) {
            FileConfiguration defaultMessages = YamlConfiguration.loadConfiguration(inputStream);

            boolean needsSave = false;

            // Check each message and set defaults if not present
            Set<String> keys = defaultMessages.getKeys(true);
            for (String key : keys) {
                if (!messages.contains(key)) {
                    messages.set(key, defaultMessages.get(key));
                    needsSave = true;
                }
            }

            if (needsSave) {
                saveMessages();
            }
        }
    }

    public void saveMessages() {
        try {
            messages.save(messagesFile);
        } catch (Exception ex) {
            plugin.getLogger().severe("Could not save messages to " + messagesFile.getName() + ": " + ex.getMessage());
        }
    }

    public Component getMessage(String id, Object... placeholders) {
        String message = messages.getString(id, "&cMissing message: " + id);

        // Apply placeholders
        if (placeholders.length > 0) {
            for (int i = 0; i < placeholders.length; i += 2) {
                if (i + 1 < placeholders.length) {
                    String key = placeholders[i].toString();
                    String value = placeholders[i + 1].toString();
                    message = message.replace("%" + key + "%", value);
                }
            }
        }

        // Apply prefix if needed
        if (!id.equals(GENERAL_PREFIX) && message.contains("%prefix%")) {
            String prefix = messages.getString(GENERAL_PREFIX, "&c[CSSPVP]");
            message = message.replace("%prefix%", prefix);
        }

        // Convert to Component with colors
        return plugin.colorize(message);
    }

    public String getMessageString(String id, Object... placeholders) {
        String message = messages.getString(id, "&cMissing message: " + id);

        // Apply placeholders
        if (placeholders.length > 0) {
            for (int i = 0; i < placeholders.length; i += 2) {
                if (i + 1 < placeholders.length) {
                    String key = placeholders[i].toString();
                    String value = placeholders[i + 1].toString();
                    message = message.replace("%" + key + "%", value);
                }
            }
        }

        // Apply prefix if needed
        if (!id.equals(GENERAL_PREFIX) && message.contains("%prefix%")) {
            String prefix = messages.getString(GENERAL_PREFIX, "&c[CSSPVP]");
            message = message.replace("%prefix%", prefix);
        }

        // Convert to String with colors
        return plugin.colorizeString(message);
    }
}
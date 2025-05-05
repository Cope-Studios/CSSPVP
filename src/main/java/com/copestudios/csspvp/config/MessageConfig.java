package com.copestudios.csspvp.config;

import com.copestudios.csspvp.CSSPVP;
import com.copestudios.csspvp.utils.ColorUtils;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class MessageConfig {

    private final CSSPVP plugin;
    private final Map<String, String> messages;

    public MessageConfig(CSSPVP plugin) {
        this.plugin = plugin;
        this.messages = new HashMap<>();
    }
    public void loadMessages() {
        FileConfiguration config = plugin.getConfigManager().getMessages();
        plugin.getLogger().info("Loading messages from messages.yml...");

        if (config.contains("messages")) {
            plugin.getLogger().info("Found messages section");
            loadMessagesRecursively(config, "messages");
        } else {
            plugin.getLogger().info("No messages section found, setting up defaults");
            setupDefaultMessages();
        }

        // Test if messages were loaded
        plugin.getLogger().info("Test message 'plugin.prefix': " +
                messages.getOrDefault("plugin.prefix", "NOT FOUND"));
        plugin.getLogger().info("Test message 'arena.create.start': " +
                messages.getOrDefault("arena.create.start", "NOT FOUND"));
    }

    private void loadMessagesRecursively(FileConfiguration config, String path) {
        if (config.isConfigurationSection(path)) {
            for (String key : config.getConfigurationSection(path).getKeys(false)) {
                String newPath = path + "." + key;
                loadMessagesRecursively(config, newPath);
            }
        } else {
            // This is a leaf node (actual message)
            String message = config.getString(path);
            if (message != null) {
                // Remove the "messages." prefix for the key
                String messageKey = path.substring("messages.".length());
                messages.put(messageKey, ColorUtils.colorize(message));
                plugin.getLogger().info("Loaded message: " + messageKey + " = " + message);
            }
        }
    }
    private void setupDefaultMessages() {
        FileConfiguration config = plugin.getConfigManager().getMessages();

        // General messages
        addMessage(config, "plugin.prefix", "&8[&bCSSPVP&8] &r");
        addMessage(config, "plugin.no-permission", "&cYou don't have permission to use this command.");
        addMessage(config, "plugin.player-only", "&cThis command can only be used by players.");
        addMessage(config, "plugin.invalid-args", "&cInvalid arguments. Usage: {usage}");

        // Lobby messages
        addMessage(config, "lobby.set", "&aLobby location has been set.");
        addMessage(config, "lobby.teleport", "&aYou have been teleported to the lobby.");
        addMessage(config, "lobby.not-set", "&cLobby location is not set.");

        // Arena messages
        addMessage(config, "arena.create.start", "&aCreating arena. Please type the name in chat.");
        addMessage(config, "arena.create.corners", "&aPlease mark corner {number} by typing 'here' in chat.");
        addMessage(config, "arena.create.spawn", "&aPlease mark the spawn point by typing 'here' in chat.");
        addMessage(config, "arena.create.success", "&aArena &b{name} &ahas been created successfully.");
        addMessage(config, "arena.setup.start", "&aSetup for arena &b{name} &ahas started.");
        addMessage(config, "arena.setup.lives", "&aHow many lives can a player have? (1-10 or 'infinite')");
        addMessage(config, "arena.setup.drop-items", "&aCan players drop or pick up items? (yes/no)");
        addMessage(config, "arena.setup.notch-apple", "&aEnter Notch Apple delay in seconds:");
        addMessage(config, "arena.setup.enderpearl", "&aEnter Enderpearl delay in seconds:");
        addMessage(config, "arena.setup.max-players", "&aHow many players can join? (1-100 or 'unlimited')");
        addMessage(config, "arena.setup.success", "&aSetup for arena &b{name} &acompleted successfully.");
        addMessage(config, "arena.join", "&aYou have joined arena &b{name}&a.");
        addMessage(config, "arena.leave", "&aYou have left arena &b{name}&a.");
        addMessage(config, "arena.full", "&cArena &b{name} &cis full.");
        addMessage(config, "arena.not-found", "&cArena &b{name} &cnot found.");
        addMessage(config, "arena.already-in", "&cYou are already in an arena.");
        addMessage(config, "arena.not-in", "&cYou are not in an arena.");

        // Kit messages
        addMessage(config, "kit.set", "&aKit for arena &b{name} &ahas been set.");
        addMessage(config, "kit.given", "&aKit for arena &b{name} &ahas been given to you.");
        addMessage(config, "kit.delay", "&aKit will be given in &b{seconds} &aseconds.");

        // Duel messages
        addMessage(config, "duel.request.sent", "&aYou have sent a duel request to &b{player}&a.");
        addMessage(config, "duel.request.received", "&b{player} &ahas sent you a duel request. Type &b/csd accept &ato accept.");
        addMessage(config, "duel.request.timeout", "&cDuel request from &b{player} &chas timed out.");
        addMessage(config, "duel.request.accepted", "&aYou have accepted the duel request from &b{player}&a.");
        addMessage(config, "duel.request.accepted-sender", "&b{player} &ahas accepted your duel request.");
        addMessage(config, "duel.countdown", "&aDuel starts in &b{seconds} &aseconds.");
        addMessage(config, "duel.started", "&aDuel has started!");
        addMessage(config, "duel.won", "&b{player} &ahas won the duel against &b{opponent}&a!");
        addMessage(config, "duel.forfeited", "&b{player} &chas forfeited the duel.");
        addMessage(config, "duel.stopped", "&cDuel has been stopped by an admin.");
        addMessage(config, "duel.not-in", "&cYou are not in a duel.");
        addMessage(config, "duel.no-zones", "&cNo duel zones available.");

        // Random duel messages
        addMessage(config, "random-duel.selected", "&aYou have been selected for a random duel against &b{player}&a.");
        addMessage(config, "random-duel.countdown", "&aRandom duel starts in &b{seconds} &aseconds.");
        addMessage(config, "random-duel.started", "&aRandom duel has started!");
        addMessage(config, "random-duel.not-enough", "&cNot enough players in arena &b{name} &cfor a random duel.");

        plugin.getConfigManager().saveMessages();
    }

    private void addMessage(FileConfiguration config, String key, String defaultValue) {
        config.set("messages." + key, defaultValue);
        messages.put(key, ColorUtils.colorize(defaultValue));
    }

    public String getMessage(String key) {
        return messages.getOrDefault(key, "&cMissing message: " + key);
    }

    public String getMessage(String key, Map<String, String> placeholders) {
        String message = getMessage(key);

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }

        return message;
    }
}
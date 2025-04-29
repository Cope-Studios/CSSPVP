package com.copestudios.csspvp;

import com.copestudios.csspvp.arena.ArenaManager;
import com.copestudios.csspvp.commands.AdminCommands;
import com.copestudios.csspvp.commands.ArenaCommands;
import com.copestudios.csspvp.commands.DuelCommands;
import com.copestudios.csspvp.commands.TeamCommands;
import com.copestudios.csspvp.config.ConfigManager;
import com.copestudios.csspvp.gui.GuiListener;
import com.copestudios.csspvp.gui.GuiManager;
import com.copestudios.csspvp.listeners.PlayerListener;
import com.copestudios.csspvp.messages.MessageManager;
import com.copestudios.csspvp.team.TeamManager;
import org.bukkit.plugin.java.JavaPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.md_5.bungee.api.ChatColor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CSSPVP extends JavaPlugin {
    private static CSSPVP instance;
    private static final Component PREFIX = Component.text("[CSSPVP] ")
            .color(TextColor.color(0xFF5555));

    private ConfigManager configManager;
    private MessageManager messageManager;
    private ArenaManager arenaManager;
    private TeamManager teamManager;
    private GuiManager guiManager;
    private GuiListener guiListener;

    // Pattern for hex color codes like &#RRGGBB
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    @Override
    public void onEnable() {
        instance = this;

        // Initialize managers
        configManager = new ConfigManager(this);
        messageManager = new MessageManager(this);
        arenaManager = new ArenaManager(this);
        teamManager = new TeamManager(this);
        guiManager = new GuiManager(this);

        // Register GUI listener
        guiListener = new GuiListener(this);

        // Register commands
        getCommand("csspvp").setExecutor(new AdminCommands(this));
        getCommand("cssarena").setExecutor(new ArenaCommands(this));
        getCommand("cssteam").setExecutor(new TeamCommands(this));
        getCommand("cssduel").setExecutor(new DuelCommands(this));

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        // Load data
        configManager.loadConfig();
        messageManager.loadMessages();
        arenaManager.loadArenas();
        teamManager.loadTeams();

        getLogger().info("CSSPVP has been enabled!");
    }

    @Override
    public void onDisable() {
        // Save data
        arenaManager.saveArenas();
        teamManager.saveTeams();

        getLogger().info("CSSPVP has been disabled!");
    }

    public Component colorize(String text) {
        // First, handle legacy color codes (&)
        String legacyConverted = ChatColor.translateAlternateColorCodes('&', text);

        // Then handle hex color codes (&#RRGGBB)
        Matcher matcher = HEX_PATTERN.matcher(legacyConverted);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hexCode = matcher.group(1);
            matcher.appendReplacement(buffer, ChatColor.of("#" + hexCode).toString());
        }
        matcher.appendTail(buffer);

        // Finally convert to a Component using MiniMessage
        return MiniMessage.miniMessage().deserialize(buffer.toString());
    }

    public String colorizeString(String text) {
        // Handle legacy color codes (&)
        String legacyConverted = ChatColor.translateAlternateColorCodes('&', text);

        // Handle hex color codes (&#RRGGBB)
        Matcher matcher = HEX_PATTERN.matcher(legacyConverted);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hexCode = matcher.group(1);
            matcher.appendReplacement(buffer, ChatColor.of("#" + hexCode).toString());
        }
        matcher.appendTail(buffer);

        return buffer.toString();
    }

    public static CSSPVP getInstance() {
        return instance;
    }

    public static Component getPrefix() {
        return PREFIX;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public TeamManager getTeamManager() {
        return teamManager;
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }
}
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
        return MiniMessage.miniMessage().deserialize(text);
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
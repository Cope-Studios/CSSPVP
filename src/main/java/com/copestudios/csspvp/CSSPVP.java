package com.copestudios.csspvp;

import com.copestudios.csspvp.commands.*;
import com.copestudios.csspvp.config.ArenaConfig;
import com.copestudios.csspvp.config.ConfigManager;
import com.copestudios.csspvp.config.MessageConfig;
import com.copestudios.csspvp.listeners.*;
import com.copestudios.csspvp.managers.*;
import com.copestudios.csspvp.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class CSSPVP extends JavaPlugin {

    private static CSSPVP instance;
    private ConfigManager configManager;
    private ArenaManager arenaManager;
    private DuelManager duelManager;
    private RandomDuelManager randomDuelManager;
    private KitManager kitManager;
    private MessageConfig messageConfig;
    private ConsoleCommandSender console;

    @Override
    public void onEnable() {
        instance = this;
        console = Bukkit.getConsoleSender();

        // Initialize configurations
        configManager = new ConfigManager(this);
        configManager.setupConfigs();

        // Initialize message system
        messageConfig = new MessageConfig(this);
        messageConfig.loadMessages();

        // Initialize managers
        arenaManager = new ArenaManager(this);
        duelManager = new DuelManager(this);
        randomDuelManager = new RandomDuelManager(this);
        kitManager = new KitManager(this);

        // Register commands
        registerCommands();

        // Register event listeners
        registerListeners();

        console.sendMessage(ColorUtils.colorize("&a[CSSPVP] Plugin enabled successfully!"));
    }

    @Override
    public void onDisable() {
        // Save all data
        configManager.saveAllConfigs();
        arenaManager.saveArenas();
        duelManager.saveDuelData();

        // Clean up resources
        arenaManager.cleanUp();
        duelManager.cleanUp();
        randomDuelManager.cleanUp();

        console.sendMessage(ColorUtils.colorize("&c[CSSPVP] Plugin disabled!"));
        instance = null;
    }

    private void registerCommands() {
        // Register main command and subcommands
        CSPCommand mainCommand = new CSPCommand(this);
        getCommand("csp").setExecutor(mainCommand);
        getCommand("csp").setTabCompleter(mainCommand);

        // Register arena commands
        CSACommand arenaCommand = new CSACommand(this);
        getCommand("csa").setExecutor(arenaCommand);
        getCommand("csa").setTabCompleter(arenaCommand);

        // Register duel commands
        CSDCommand duelCommand = new CSDCommand(this);
        getCommand("csd").setExecutor(duelCommand);
        getCommand("csd").setTabCompleter(duelCommand);

        // Register random duel commands
        CSRDCommand randomDuelCommand = new CSRDCommand(this);
        getCommand("csrd").setExecutor(randomDuelCommand);
        getCommand("csrd").setTabCompleter(randomDuelCommand);
    }

    private void registerListeners() {
        // Register event listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new ArenaListener(this), this);
        getServer().getPluginManager().registerEvents(new DuelListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
    }

    public static CSSPVP getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public DuelManager getDuelManager() {
        return duelManager;
    }

    public RandomDuelManager getRandomDuelManager() {
        return randomDuelManager;
    }

    public KitManager getKitManager() {
        return kitManager;
    }

    public MessageConfig getMessageConfig() {
        return messageConfig;
    }
}
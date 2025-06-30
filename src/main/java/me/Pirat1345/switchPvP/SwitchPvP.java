package me.Pirat1345.switchPvP;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;

public final class SwitchPvP extends JavaPlugin {
    private FileConfiguration config;
    private KitManager kitManager;
    private GameManager gameManager;

    @Override
    public void onEnable() {
        // Save default config if it doesn't exist
        saveDefaultConfig();
        config = getConfig();

        // Initialize managers
        this.kitManager = new KitManager(this);
        this.gameManager = new GameManager(this);


        // Register command with tab completer
        getCommand("switchpvp").setExecutor(new SwitchPvPCommand(this));
        getCommand("switchpvp").setTabCompleter(new SwitchPvPCommand(this));

        // Register events
        getServer().getPluginManager().registerEvents(new GameListener(this), this);

        getLogger().info("SwitchPvP plugin enabled successfully!");
    }

    @Override
    public void onDisable() {
        // Save data if managers were initialized
        if (gameManager != null) {
            gameManager.saveGameData();
        }
        if (kitManager != null) {
            kitManager.saveKits();
        }
        gameManager.cleanup();

        getLogger().info("SwitchPvP plugin disabled successfully!");
    }

    public KitManager getKitManager() {
        return kitManager;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public FileConfiguration getPluginConfig() {
        return config;
    }
}
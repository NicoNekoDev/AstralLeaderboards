package ro.nico.leaderboard;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import ro.nico.leaderboard.api.BoardsManager;
import ro.nico.leaderboard.api.PlaceholderAPIHook;
import ro.nico.leaderboard.storage.StorageConfiguration;
import ro.nico.leaderboard.storage.types.Storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Objects;

public class AstralLeaderboardsPlugin extends JavaPlugin {
    private final YamlConfiguration settings = new YamlConfiguration();
    private final StorageConfiguration storage = new StorageConfiguration(this);
    private final File settingsFile = new File(this.getDataFolder(), "settings.yml");
    private final BoardsManager manager = new BoardsManager(this);
    private Permission permissions;

    @Override
    public void onEnable() {
        this.getLogger().info("Loading plugin...");
        Plugin papi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
        if (papi != null && papi.isEnabled()) {
            this.getLogger().info("Hooking into PlaceholderAPI...");
            new PlaceholderAPIHook(this).register();
        } else {
            this.getLogger().warning("Could not hook into PlaceholderAPI, disabling!");
            this.setEnabled(false);
        }
        Plugin vault = Bukkit.getPluginManager().getPlugin("Vault");
        if (vault != null && vault.isEnabled()) {
            this.getLogger().info("Hooking into Vault...");
            this.permissions = Objects.requireNonNull(this.getServer().getServicesManager().getRegistration(Permission.class), "Failed to find Vault permissions").getProvider();
        } else {
            this.getLogger().warning("Could not hook into Vault, disabling!");
            this.setEnabled(false);
        }
        this.getDataFolder().mkdirs();
        this.reloadConfig();
        try {
            this.storage.load(this.settings);
            this.manager.loadAllBoards();
        } catch (SQLException e) {
            this.getLogger().severe("Could not load storage!");
        }
        Objects.requireNonNull(this.getCommand("astrallb"), "Failed to find main command!").setExecutor(new AstralLeaderboardsCommand(this));
    }

    public Permission getVaultPermissions() {
        return this.permissions;
    }

    @Override
    public void onDisable() {
        this.manager.unloadAllBoards();
    }

    public final BoardsManager getBoardsManager() {
        return this.manager;
    }

    public void reloadPlugin() {
        this.reloadConfig();
        try {
            this.storage.unload();
            this.storage.load(this.settings);
            this.manager.unloadAllBoards();
            this.manager.loadAllBoards();
        } catch (SQLException e) {
            this.getLogger().severe("Could not reload storage!");
        }
    }

    public Storage getStorage() {
        return this.storage;
    }

    @Override
    public void reloadConfig() {
        if (!this.settingsFile.exists())
            this.saveResource("settings.yml", false);
        try {
            this.settings.load(this.settingsFile);
        } catch (IOException | InvalidConfigurationException e) {
            this.getLogger().severe("Could not load settings.yml!");
            try {
                try (InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(this.getResource("settings.yml")))) {
                    try (BufferedReader bufferedReader = new BufferedReader(reader)) {
                        this.settings.load(bufferedReader);
                    }
                }
            } catch (IOException | InvalidConfigurationException ex) {
                this.getLogger().severe("Could not load default settings.yml!");
            }
        }
    }

    @NotNull
    @Override
    public FileConfiguration getConfig() {
        return this.settings;
    }

    @Override
    public void saveConfig() {
        try {
            this.settings.save(this.settingsFile);
        } catch (IOException e) {
            this.getLogger().severe("Could not save settings.yml!");
        }
    }
}

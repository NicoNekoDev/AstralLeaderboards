package ro.nico.leaderboard;

import com.mrivanplays.annotationconfig.core.resolver.ConfigResolver;
import com.mrivanplays.annotationconfig.yaml.YamlConfig;
import lombok.Getter;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Objects;

public class AstralLeaderboardsPlugin extends JavaPlugin {
    private final YamlConfiguration settings = new YamlConfiguration();
    @Getter private final StorageConfiguration storage = new StorageConfiguration(this);
    private final File settingsFile = new File(this.getDataFolder(), "settings.yml");
    @Getter private final BoardsManager boardsManager = new BoardsManager(this);
    @Getter private final ConfigResolver configResolver = YamlConfig.getConfigResolver();
    @Getter private Permission vaultPermissions;

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
            this.vaultPermissions = Objects.requireNonNull(this.getServer().getServicesManager().getRegistration(Permission.class), "Failed to find Vault permissions").getProvider();
        } else {
            this.getLogger().warning("Could not hook into Vault, disabling!");
            this.setEnabled(false);
        }
        this.getDataFolder().mkdirs();
        this.reloadConfig();
        try {
            this.storage.load(this.settings);
            this.boardsManager.loadAllBoards();
        } catch (SQLException e) {
            this.getLogger().severe("Could not load storage!");
        }
        Objects.requireNonNull(this.getCommand("astrallb"), "Failed to find main command!").setExecutor(new AstralLeaderboardsCommand(this));
    }

    @Override
    public void onDisable() {
        this.boardsManager.unloadAllBoards();
    }

    public void reloadPlugin() {
        this.reloadConfig();
        try {
            this.storage.unload();
            this.storage.load(this.settings);
            this.boardsManager.unloadAllBoards();
            this.boardsManager.loadAllBoards();
        } catch (SQLException e) {
            this.getLogger().severe("Could not reload storage!");
        }
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

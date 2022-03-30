package ro.nico.leaderboard;

import com.mrivanplays.annotationconfig.core.resolver.ConfigResolver;
import com.mrivanplays.annotationconfig.yaml.YamlConfig;
import lombok.Getter;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import ro.nico.leaderboard.api.BoardsManager;
import ro.nico.leaderboard.api.PlaceholderAPIHook;
import ro.nico.leaderboard.listener.PlayerEvents;
import ro.nico.leaderboard.storage.StorageConfiguration;
import ro.nico.leaderboard.settings.PluginSettings;

import java.io.File;
import java.sql.SQLException;
import java.util.Objects;

public class AstralLeaderboardsPlugin extends JavaPlugin {
    @Getter private final PluginSettings settings = new PluginSettings();
    @Getter private final StorageConfiguration storage = new StorageConfiguration(this);
    private final File settingsFile = new File(this.getDataFolder(), "settings.yml");
    @Getter private final BoardsManager boardsManager = new BoardsManager(this);
    @Getter private final ConfigResolver configResolver = YamlConfig.getConfigResolver();
    @Getter private Permission vaultPermissions;

    @Override
    public void onEnable() {
        this.getLogger().info("Loading plugin...");
        this.loadDependencies();
        this.reloadPlugin();
        Objects.requireNonNull(this.getCommand("astrallb"), "Failed to find main command!").setExecutor(new AstralLeaderboardsCommand(this));
        Bukkit.getPluginManager().registerEvents(new PlayerEvents(this), this);
    }

    @Override
    public void onDisable() {
        this.boardsManager.unloadAllBoards();
        try {
            this.storage.unload();
        } catch (SQLException e) {
            this.getLogger().severe("Failed to unload storage!");
        }
    }

    private void loadDependencies() {
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
    }

    public void reloadPlugin() {
        this.getDataFolder().mkdirs();
        this.configResolver.loadOrDump(this.settings, this.settingsFile);
        try {
            this.storage.unload();
            this.storage.load(this.settings);
            this.boardsManager.unloadAllBoards();
            this.boardsManager.loadAllBoards();
        } catch (SQLException e) {
            this.getLogger().severe("Could not load storage!");
        }
    }
}

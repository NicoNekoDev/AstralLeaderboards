package ro.nico.leaderboard;

import lombok.Getter;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import ro.nico.leaderboard.api.BoardsManager;
import ro.nico.leaderboard.api.PlaceholderAPIHook;
import ro.nico.leaderboard.settings.PluginSettings;
import ro.nico.leaderboard.storage.StorageConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class AstralLeaderboardsPlugin extends JavaPlugin {
    private final File settingsFile = new File(getDataFolder(), "settings.yml");
    private final File cache = new File(getDataFolder(), UUID.randomUUID() + ".cache");
    @Getter private PluginSettings settings;
    @Getter private final StorageConfiguration storage = new StorageConfiguration(this);
    @Getter private final BoardsManager boardsManager = new BoardsManager(this);
    @Getter private Permission vaultPerms;
    @Getter private DB mapDB;

    @Override
    public void onEnable() {
        this.getLogger().info("Loading plugin...");
        this.loadDependencies();
        this.reloadPlugin();
        Objects.requireNonNull(this.getCommand("astrallb"), "Failed to find main command!").setExecutor(new AstralLeaderboardsCommand(this));
    }

    @Override
    public void onDisable() {
        this.boardsManager.unloadAllBoards();
        try {
            this.storage.unload();
        } catch (SQLException e) {
            this.getLogger().severe("Failed to unload storage!");
        }
        if (mapDB != null)
            mapDB.close();
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
            this.vaultPerms = Objects.requireNonNull(this.getServer().getServicesManager().getRegistration(Permission.class), "Failed to find Vault permissions").getProvider();
        } else {
            this.getLogger().warning("Could not hook into Vault, disabling!");
            this.setEnabled(false);
        }
    }

    public void reloadPlugin() {
        if (mapDB != null)
            mapDB.close();
        mapDB = DBMaker.fileDB(this.cache).closeOnJvmShutdown().fileDeleteAfterClose().fileMmapEnableIfSupported().make();
        this.getDataFolder().mkdirs();
        try {
            Files.newDirectoryStream(this.getDataFolder().toPath(), (path) -> path.toString().endsWith(".cache")).forEach((path) -> path.toFile().delete());
        } catch (IOException ignore) {
        }
        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(this.settingsFile);
            config.options().setHeader(
                    List.of(
                            "",
                            "               _             _ _                    _           _                         _     ",
                            "     /\\       | |           | | |                  | |         | |                       | |    ",
                            "    /  \\   ___| |_ _ __ __ _| | |     ___  __ _  __| | ___ _ __| |__   ___   __ _ _ __ __| |___ ",
                            "   / /\\ \\ / __| __| '__/ _` | | |    / _ \\/ _` |/ _` |/ _ \\ '__| '_ \\ / _ \\ / _` | '__/ _` / __|",
                            "  / ____ \\\\__ \\ |_| | | (_| | | |___|  __/ (_| | (_| |  __/ |  | |_) | (_) | (_| | | | (_| \\__ \\",
                            " /_/    \\_\\___/\\__|_|  \\__,_|_|______\\___|\\__,_|\\__,_|\\___|_|  |_.__/ \\___/ \\__,_|_|  \\__,_|___/",
                            ""
                    )
            );
            this.settings = new PluginSettings();
            this.settings.load(config);
            config.save(settingsFile);
        } catch (IOException e) {
            this.getLogger().severe("Failed to load settings!");
        }
        try {
            this.storage.unload();
            this.storage.load(this.settings.getStorageSettings());
            this.boardsManager.unloadAllBoards();
            this.boardsManager.loadAllBoards();
        } catch (SQLException e) {
            this.getLogger().severe("Could not load storage! " + e.getMessage());
        }
    }

    public boolean hasPermission(CommandSender sender, String permission) {
        return this.vaultPerms.has(sender, permission);
    }
}

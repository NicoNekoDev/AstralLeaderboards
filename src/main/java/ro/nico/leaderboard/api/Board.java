package ro.nico.leaderboard.api;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import ro.nico.leaderboard.AstralLeaderboardsPlugin;
import ro.nico.leaderboard.settings.BoardSettings;
import ro.nico.leaderboard.storage.cache.BoardData;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class Board {
    private final File boardFile;
    @Getter private final AstralLeaderboardsPlugin plugin;
    @Getter private final BoardData boardData;
    @Getter private final String id;
    @Getter private final BoardSettings boardSettings;
    private BukkitTask updateTask;
    private BukkitTask asyncHeartbeatTask;
    private BukkitTask syncHeartbeatTask;

    public Board(@NotNull AstralLeaderboardsPlugin plugin, @NotNull String id, @NotNull File boardFile, @NotNull BoardSettings boardSettings) {
        this.plugin = plugin;
        this.id = id;
        this.boardFile = boardFile;
        this.boardSettings = boardSettings;
        this.boardData = new BoardData(this); // KEEP ORDER
    }

    public Board addTracker(@NotNull String trackerId, @NotNull String trackerPlaceholder) {
        this.boardSettings.getTrackers().set(trackerId, trackerPlaceholder);
        return this;
    }

    public Board removeTracker(@NotNull String trackerId) {
        this.boardSettings.getTrackers().set(trackerId, null);
        return this;
    }

    public Board addTrackers(@NotNull Map<String, String> trackers) {
        for (Map.Entry<String, String> entry : trackers.entrySet()) {
            this.boardSettings.getTrackers().set(entry.getKey(), entry.getValue());
        }
        return this;
    }

    public boolean hasPlayerExempt(@NotNull String name) {
        return this.boardSettings.getExemptPlayers().contains(name);
    }

    public Board addExemptPlayer(@NotNull String playerName) {
        this.boardSettings.getExemptPlayers().add(playerName);
        return this;
    }

    @NotNull
    public Map<String, String> getTrackers() {
        ConfigurationSection trackers = this.boardSettings.getTrackers();
        ImmutableMap.Builder<String, String> immutable = ImmutableMap.builder();
        for (String key : trackers.getKeys(false)) {
            if (trackers.isString(key)) {
                immutable.put(key, trackers.getString(key, "null"));
            }
        }
        return immutable.build();
    }

    protected void loadSettings() {
        try {
            YamlConfiguration config = new YamlConfiguration();
            if (this.boardFile.exists())
                config.load(this.boardFile);
            this.boardSettings.load(config);
        } catch (IOException | InvalidConfigurationException e) {
            this.plugin.getLogger().severe("Failed to load board settings for board " + this.id + "!");
        }
    }

    protected void saveSettings() {
        try {
            YamlConfiguration config = new YamlConfiguration();
            this.boardSettings.load(config);
            config.save(this.boardFile);
        } catch (IOException e) {
            this.plugin.getLogger().severe("Failed to save board settings for board " + this.id + "!");
        }
    }

    protected void deleteSettings() {
        this.boardFile.delete();
    }

    protected void enable() {
        this.boardData.load();
        if (this.updateTask == null || updateTask.isCancelled())
            this.updateTask = Bukkit.getScheduler()
                    .runTaskTimer(this.plugin, () -> this.boardData.update(false), 1L, 20L * this.boardSettings.getUpdateInterval());
        if (this.asyncHeartbeatTask == null || asyncHeartbeatTask.isCancelled())
            this.asyncHeartbeatTask = Bukkit.getScheduler()
                    .runTaskTimerAsynchronously(this.plugin, this.boardData::asyncHeartbeat, 20L, this.boardSettings.getHeartbeatInterval());
        if (this.syncHeartbeatTask == null || syncHeartbeatTask.isCancelled())
            this.syncHeartbeatTask = Bukkit.getScheduler()
                    .runTaskTimer(this.plugin, this.boardData::syncHeartbeat, 40L, this.boardSettings.getHeartbeatInterval());
    }

    protected void disable() {
        if (this.asyncHeartbeatTask != null) this.asyncHeartbeatTask.cancel();
        if (this.syncHeartbeatTask != null) this.syncHeartbeatTask.cancel();
        if (this.updateTask != null) this.updateTask.cancel();
        this.boardData.unload();
    }
}

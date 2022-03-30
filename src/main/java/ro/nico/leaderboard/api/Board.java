package ro.nico.leaderboard.api;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import ro.nico.leaderboard.AstralLeaderboardsPlugin;
import ro.nico.leaderboard.storage.cache.BoardData;
import ro.nico.leaderboard.settings.BoardSettings;

import java.io.File;
import java.util.Map;
import java.util.UUID;

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
        this.boardData = new BoardData(this);
        this.id = id;
        this.boardFile = boardFile;
        this.boardSettings = boardSettings;
    }

    public Board addTracker(@NotNull String trackerId, @NotNull String trackerPlaceholder) {
        this.getTrackers().put(trackerId, trackerPlaceholder);
        return this;
    }

    public Board removeTracker(@NotNull String trackerId) {
        this.getTrackers().remove(trackerId);
        return this;
    }

    public Board addTrackers(@NotNull Map<String, String> trackers) {
        this.getTrackers().putAll(trackers);
        return this;
    }

    public boolean hasPlayerExempt(@NotNull String name) {
        return this.boardSettings.getExemptPlayersNames().contains(name);
    }

    public boolean hasPlayerExempt(@NotNull UUID playerUUID) {
        return this.boardSettings.getExemptPlayersUUIDs().contains(playerUUID.toString());
    }

    public Board addExemptPlayer(@NotNull String playerName) {
        this.boardSettings.getExemptPlayersNames().add(playerName);
        return this;
    }

    public Board addExemptPlayer(@NotNull UUID playerUUID) {
        this.boardSettings.getExemptPlayersUUIDs().add(playerUUID.toString());
        return this;
    }

    @NotNull
    public Map<String, Object> getTrackers() {
        return this.boardSettings.getTrackers();
    }

    protected void loadSettings() {
        this.plugin.getConfigResolver().load(this.boardSettings, this.boardFile);
    }

    protected void dumpSettings() {
        this.plugin.getConfigResolver().dump(this.boardSettings, this.boardFile);
    }

    protected void enable() {
        this.asyncHeartbeatTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this.plugin, this.boardData::asyncHeartbeat, this.boardSettings.getHeartbeatInterval(), this.boardSettings.getHeartbeatInterval());
        this.syncHeartbeatTask = Bukkit.getScheduler().runTaskTimer(this.plugin, this.boardData::syncHeartbeat, this.boardSettings.getHeartbeatInterval(), this.boardSettings.getHeartbeatInterval());
        this.updateTask = Bukkit.getScheduler().runTaskTimer(this.plugin, this.boardData::update, 20L * this.boardSettings.getUpdateInterval(), 20L * this.boardSettings.getUpdateInterval());
    }

    protected void disable() {
        if (this.asyncHeartbeatTask != null) this.asyncHeartbeatTask.cancel();
        if (this.syncHeartbeatTask != null) this.syncHeartbeatTask.cancel();
        if (this.updateTask != null) this.updateTask.cancel();
    }

    protected void deleteSettings() {
        this.boardFile.delete();
    }
}

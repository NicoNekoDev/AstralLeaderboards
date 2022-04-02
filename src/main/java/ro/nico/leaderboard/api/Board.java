package ro.nico.leaderboard.api;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import ro.nico.leaderboard.AstralLeaderboardsPlugin;
import ro.nico.leaderboard.storage.cache.BoardData;
import ro.nico.leaderboard.settings.BoardSettings;
import ro.nico.leaderboard.util.GsonUtil;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class Board {
    private final File boardFile;
    @Getter private final AstralLeaderboardsPlugin plugin;
    @Getter private final BoardData boardData;
    @Getter private final String id;
    @Getter private BoardSettings boardSettings;
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
        return this.boardSettings.getExemptPlayersSettings().getExemptPlayersNames().contains(name);
    }

    public boolean hasPlayerExempt(@NotNull UUID playerUUID) {
        return this.boardSettings.getExemptPlayersSettings().getExemptPlayersUUIDs().contains(playerUUID.toString());
    }

    public Board addExemptPlayer(@NotNull String playerName) {
        this.boardSettings.getExemptPlayersSettings().getExemptPlayersNames().add(playerName);
        return this;
    }

    public Board addExemptPlayer(@NotNull UUID playerUUID) {
        this.boardSettings.getExemptPlayersSettings().getExemptPlayersUUIDs().add(playerUUID.toString());
        return this;
    }

    @NotNull
    public Map<String, String> getTrackers() {
        return this.boardSettings.getTrackers();
    }

    protected void loadSettings() {
        try {
            this.boardSettings = GsonUtil.load(BoardSettings.class, this.boardFile);
        } catch (IOException e) {
            this.plugin.getLogger().severe("Failed to load board settings for board " + this.id + "!");
        }
    }

    protected void saveSettings() {
        try {
            GsonUtil.save(this.boardSettings, this.boardFile);
        } catch (IOException e) {
            this.plugin.getLogger().severe("Failed to save board settings for board " + this.id + "!");
        }
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

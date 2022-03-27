package ro.nico.leaderboard.api;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import ro.nico.leaderboard.AstralLeaderboardsPlugin;
import ro.nico.leaderboard.storage.cache.BoardData;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Board {
    private final AstralLeaderboardsPlugin plugin;
    private final BoardData data;
    private final String id;
    private final File boardFile;
    private final YamlConfiguration boardConfig;
    private BukkitTask updateTask;
    private BukkitTask asyncHeartbeatTask;
    private BukkitTask syncHeartbeatTask;

    public Board(@NotNull AstralLeaderboardsPlugin plugin, @NotNull String id, @NotNull File boardFile, @NotNull YamlConfiguration boardConfig) {
        this.plugin = plugin;
        this.data = new BoardData(this);
        this.id = id;
        this.boardFile = boardFile;
        this.boardConfig = boardConfig;
    }

    @NotNull
    public BoardData getBoardData() {
        return this.data;
    }

    @NotNull
    public AstralLeaderboardsPlugin getPlugin() {
        return plugin;
    }

    @NotNull
    public String getId() {
        return this.id;
    }

    @NotNull
    public String getSorter() {
        return this.boardConfig.getString("sorter", "%player_name%");
    }

    public Board setSorter(@NotNull String sorter) {
        this.boardConfig.set("sorter", sorter);
        return this;
    }

    public int getUpdateInterval() {
        return this.boardConfig.getInt("update-interval", 30);
    }

    public Board setUpdateInterval(int updateInterval) throws IllegalArgumentException {
        if (updateInterval < this.getHeartbeatInterval())
            throw new IllegalArgumentException("Update interval cannot be less than heartbeat interval");
        this.boardConfig.set("update-interval", updateInterval);
        return this;
    }

    public int getHeartbeatInterval() {
        return this.boardConfig.getInt("heartbeat-interval", 20);
    }

    public Board setHeartbeatInterval(int heartbeatInterval) throws IllegalArgumentException {
        if (heartbeatInterval > this.getUpdateInterval())
            throw new IllegalArgumentException("Heartbeat interval cannot be greater than update interval");
        this.boardConfig.set("heartbeat-interval", heartbeatInterval);
        return this;
    }

    public Board addTracker(@NotNull String trackerId, @NotNull String trackerPlaceholder) {
        this.getTrackers().set(trackerId, trackerPlaceholder);
        return this;
    }

    public Board removeTracker(@NotNull String trackerId) {
        this.getTrackers().set(trackerId, null);
        return this;
    }

    public Board addTrackers(@NotNull Map<String, String> trackers) {
        for (Map.Entry<String, String> entry : trackers.entrySet())
            this.getTrackers().set(entry.getKey(), entry.getValue());
        return this;
    }

    public boolean hasPlayerExempt(@NotNull String name) {
        return this.getExemptPlayersNames().contains(name);
    }

    public boolean hasPlayerExempt(@NotNull UUID playerUUID) {
        return this.getExemptPlayersUUIDs().contains(playerUUID.toString());
    }

    public Board addExemptPlayer(@NotNull String playerName) {
        this.getExemptPlayersNames().add(playerName);
        return this;
    }

    public Board addExemptPlayer(@NotNull UUID playerUUID) {
        this.getExemptPlayersUUIDs().add(playerUUID.toString());
        return this;
    }

    @SuppressWarnings("ConstantConditions")
    @NotNull
    public ConfigurationSection getTrackers() {
        return this.boardConfig.isConfigurationSection("trackers") ? this.boardConfig.getConfigurationSection("trackers") : this.boardConfig.createSection("trackers");
    }

    @NotNull
    public List<String> getExemptPlayersUUIDs() {
        if (!this.boardConfig.isList("exempt-players.uuids")) {
            this.boardConfig.set("exempt-players.uuids", new ArrayList<String>());
        }
        return this.boardConfig.getStringList("exempt-players.uuids");
    }

    @NotNull
    public List<String> getExemptPlayersNames() {
        if (!this.boardConfig.isList("exempt-players.names")) {
            this.boardConfig.set("exempt-players.names", new ArrayList<String>());
        }
        return this.boardConfig.getStringList("exempt-players.names");
    }

    public Board setReversed(boolean reversed) {
        this.boardConfig.set("reversed", reversed);
        return this;
    }

    public boolean isReversed() {
        return this.boardConfig.getBoolean("reversed", false);
    }

    protected void loadConfig() throws IOException, InvalidConfigurationException {
        this.boardConfig.load(this.boardFile);
    }

    protected void saveConfig() throws IOException {
        this.boardConfig.save(this.boardFile);
    }

    protected void enable() {
        this.asyncHeartbeatTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this.plugin, this.data::asyncHeartbeat, this.getHeartbeatInterval(), this.getHeartbeatInterval());
        this.syncHeartbeatTask = Bukkit.getScheduler().runTaskTimer(this.plugin, this.data::syncHeartbeat, this.getHeartbeatInterval(), this.getHeartbeatInterval());
        this.updateTask = Bukkit.getScheduler().runTaskTimer(this.plugin, this.data::update, 20L * this.getUpdateInterval(), 20L * this.getUpdateInterval());
    }

    protected void disable() {
        if (this.asyncHeartbeatTask != null) this.asyncHeartbeatTask.cancel();
        if (this.syncHeartbeatTask != null) this.syncHeartbeatTask.cancel();
        if (this.updateTask != null) this.updateTask.cancel();
    }

    public int getRowSize() {
        return this.boardConfig.getInt("row-size", 25);
    }

    public Board setRowSize(int rowSize) {
        this.boardConfig.set("row-size", rowSize);
        return this;
    }

    public String getDefaultPlaceholder() {
        return this.boardConfig.getString("default-placeholder", "---");
    }

    public Board setDefaultPlaceholder(String placeholder) {
        this.boardConfig.set("default-placeholder", placeholder);
        return this;
    }

    public boolean isHourlyUpdateEnabled() {
        return this.boardConfig.getBoolean("hourly-update", false);
    }

    public Board setHourlyUpdateEnabled(boolean enabled) {
        this.boardConfig.set("hourly-update", enabled);
        return this;
    }

    public boolean isDailyUpdateEnabled() {
        return this.boardConfig.getBoolean("daily-update", false);
    }

    public Board setDailyUpdateEnabled(boolean enabled) {
        this.boardConfig.set("daily-update", enabled);
        return this;
    }

    public boolean isWeeklyUpdateEnabled() {
        return this.boardConfig.getBoolean("weekly-update", false);
    }

    public Board setWeeklyUpdateEnabled(boolean enabled) {
        this.boardConfig.set("weekly-update", enabled);
        return this;
    }

    public boolean isMonthlyUpdateEnabled() {
        return this.boardConfig.getBoolean("monthly-update", false);
    }

    public Board setMonthlyUpdateEnabled(boolean enabled) {
        this.boardConfig.set("monthly-update", enabled);
        return this;
    }

    public boolean isYearlyUpdateEnabled() {
        return this.boardConfig.getBoolean("yearly-update", false);
    }

    public Board setYearlyUpdateEnabled(boolean enabled) {
        this.boardConfig.set("yearly-update", enabled);
        return this;
    }
}

package ro.nico.leaderboard.storage.cache;

import lombok.AccessLevel;
import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.nico.leaderboard.api.Board;
import ro.nico.leaderboard.data.PlayerData;
import ro.nico.leaderboard.data.PlayerId;
import ro.nico.leaderboard.settings.UpdateSettings;
import ro.nico.leaderboard.storage.SQLDateType;
import ro.nico.leaderboard.util.RankedMapList;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

public class BoardData {
    private final Board board;
    @Getter(AccessLevel.PROTECTED) private final IdentityHashMap<SQLDateType, RankedMapList> data = new IdentityHashMap<>(8);
    private CompletableFuture<Map<PlayerId, PlayerData>> asyncFutureData;

    public BoardData(@NotNull Board board) {
        this.board = board;
    }

    public void load() throws IOException {
        if (!this.data.isEmpty())
            this.data.clear();
        this.data.put(SQLDateType.ALLTIME, new RankedMapList(this.board, SQLDateType.ALLTIME));
        UpdateSettings updateSettings = this.board.getBoardSettings().getUpdateSettings();
        if (updateSettings.isHourlyUpdated())
            this.data.put(SQLDateType.HOURLY, new RankedMapList(this.board, SQLDateType.HOURLY));
        if (updateSettings.isDailyUpdated())
            this.data.put(SQLDateType.DAILY, new RankedMapList(this.board, SQLDateType.DAILY));
        if (updateSettings.isWeeklyUpdated())
            this.data.put(SQLDateType.WEEKLY, new RankedMapList(this.board, SQLDateType.WEEKLY));
        if (updateSettings.isMonthlyUpdated())
            this.data.put(SQLDateType.MONTHLY, new RankedMapList(this.board, SQLDateType.MONTHLY));
        if (updateSettings.isYearlyUpdated())
            this.data.put(SQLDateType.YEARLY, new RankedMapList(this.board, SQLDateType.YEARLY));
    }

    public void unload() {
        if (this.asyncFutureData != null && this.asyncFutureData.cancel(false))
            this.asyncFutureData = null;
        this.data.values().forEach(RankedMapList::unload);
        this.data.clear();
    }

    @NotNull
    public PlayerData getData(int rank, @NotNull SQLDateType type) {
        if (!this.data.containsKey(type))
            return new PlayerData(board.getBoardSettings().getDefaultSorterPlaceholder(), new HashMap<>(), -1);
        if (!this.data.get(type).containsRank(rank))
            return new PlayerData(board.getBoardSettings().getDefaultSorterPlaceholder(), new HashMap<>(), -1);
        return this.data.get(type).getValueByRank(rank);
    }

    @NotNull
    public PlayerData getData(Player player, @NotNull SQLDateType type) {
        PlayerId key = new PlayerId(player.getName(), player.getUniqueId());
        if (!this.data.containsKey(type))
            return new PlayerData(board.getBoardSettings().getDefaultSorterPlaceholder(), new HashMap<>(), -1);
        if (!this.data.get(type).containsKey(key))
            return new PlayerData(board.getBoardSettings().getDefaultSorterPlaceholder(), new HashMap<>(), -1);
        return this.data.get(type).getValueByKey(key);
    }

    @Nullable
    public PlayerId getKey(int rank, @NotNull SQLDateType type) {
        if (!this.data.containsKey(type))
            return new PlayerId(board.getBoardSettings().getDefaultSorterPlaceholder(), UUID.randomUUID());
        if (!this.data.get(type).containsRank(rank))
            return new PlayerId(board.getBoardSettings().getDefaultSorterPlaceholder(), UUID.randomUUID());
        return this.data.get(type).getKeyByRank(rank);
    }

    public void asyncUpdate() {
        if (this.asyncFutureData != null && this.asyncFutureData.isDone()) {
            try {
                Map<PlayerId, PlayerData> data = asyncFutureData.get();
                this.board.getPlugin().getStorage().putDataForBoard(board, data);
                this.data.forEach((type, map) -> {
                    try {
                        map.update(this.board, type);
                    } catch (SQLException e) {
                        board.getPlugin().getLogger().log(Level.SEVERE, "Failed to async heartbeat for " + type.name() + " data!", e);
                    }
                });
                this.asyncFutureData = null;
            } catch (InterruptedException | ExecutionException | SQLException e) {
                board.getPlugin().getLogger().log(Level.SEVERE, "Error while syncing data", e);
            }
        }
    }

    public void syncUpdate(boolean forceOffline) {
        if (this.asyncFutureData == null || this.asyncFutureData.isDone()) {
            this.asyncFutureData = new CompletableFuture<>();
            Map<PlayerId, PlayerData> data = new LinkedHashMap<>();
            // players_loop label
            players_loop:
            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID uuid = player.getUniqueId();
                String name = player.getName();
                String boardSorter = this.board.getBoardSettings().getSorter();
                String sorter = PlaceholderAPI.setPlaceholders(player, boardSorter);
                if (boardSorter.equals(sorter) || sorter.isEmpty())
                    continue;
                Map<String, String> playerTrackers = new LinkedHashMap<>();
                for (String entryKey : board.getBoardSettings().getTrackers().getKeys(false)) {
                    String entryValue = board.getBoardSettings().getTrackers().getString(entryKey, "%sorter%");
                    String value = PlaceholderAPI.setPlaceholders(player, entryValue).replace("%sorter%", sorter);
                    if (entryValue.equals(value) || value.isEmpty()) // if any of the tracker is empty or return the same value,
                        continue players_loop;                       // << continue to label (skip)
                    playerTrackers.put(entryKey, value);
                }
                data.put(new PlayerId(name, uuid), new PlayerData(sorter, playerTrackers, -1));
            }
            if (forceOffline) {
                players_loop:
                for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                    UUID uuid = offlinePlayer.getUniqueId();
                    String name = offlinePlayer.getName();
                    if (name == null)
                        continue;
                    String boardSorter = this.board.getBoardSettings().getSorter();
                    String sorter = PlaceholderAPI.setPlaceholders(offlinePlayer, boardSorter);
                    if (boardSorter.equals(sorter) || sorter.isEmpty())
                        continue;
                    Map<String, String> playerTrackers = new LinkedHashMap<>();
                    for (String entryKey : board.getBoardSettings().getTrackers().getKeys(false)) {
                        String entryValue = board.getBoardSettings().getTrackers().getString(entryKey, "%sorter%");
                        String value = PlaceholderAPI.setPlaceholders(offlinePlayer, entryValue).replace("%sorter%", sorter);
                        if (entryValue.equals(value) || value.isEmpty())
                            continue players_loop; // << continue to label
                        playerTrackers.put(entryKey, value);
                    }
                    data.put(new PlayerId(name, uuid), new PlayerData(sorter, playerTrackers, -1));
                }
            }
            this.asyncFutureData.complete(data);
        }
    }
}

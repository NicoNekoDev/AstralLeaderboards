package ro.nico.leaderboard.storage.cache;

import com.google.common.collect.ImmutableMap;
import io.github.NicoNekoDev.SimpleTuples.Pair;
import lombok.AccessLevel;
import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.nico.leaderboard.api.Board;
import ro.nico.leaderboard.settings.UpdateSettings;
import ro.nico.leaderboard.storage.SQLDateType;
import ro.nico.leaderboard.util.SyncAsyncData;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

public class BoardData {
    private final Board board;
    @Getter(AccessLevel.PROTECTED) private IdentityHashMap<SQLDateType, SyncAsyncData> data;
    private CompletableFuture<Pair<Set<Pair<String, UUID>>, Map<Pair<String, UUID>, Pair<String, Map<String, String>>>>> asyncFutureData;

    public BoardData(@NotNull Board board) {
        this.board = board;
    }

    public void load() {
        this.data = new IdentityHashMap<>(8) {
            {
                put(SQLDateType.ALLTIME, new SyncAsyncData());
                UpdateSettings updateSettings = board.getBoardSettings().getUpdateSettings();
                if (updateSettings.isHourlyUpdated())
                    put(SQLDateType.HOURLY, new SyncAsyncData());
                if (updateSettings.isDailyUpdated())
                    put(SQLDateType.DAILY, new SyncAsyncData());
                if (updateSettings.isWeeklyUpdated())
                    put(SQLDateType.WEEKLY, new SyncAsyncData());
                if (updateSettings.isMonthlyUpdated())
                    put(SQLDateType.MONTHLY, new SyncAsyncData());
                if (updateSettings.isYearlyUpdated())
                    put(SQLDateType.YEARLY, new SyncAsyncData());
            }
        };
    }

    public void unload() {
        if (this.asyncFutureData != null && this.asyncFutureData.cancel(false))
            this.asyncFutureData = null;
        for (SyncAsyncData data : this.data.values())
            data.unload();
        this.data.clear();
    }

    @Nullable
    public PlayerData getData(int index, @NotNull SQLDateType type) {
        if (!this.getData().containsKey(type))
            return null;
        return this.data.get(type).getTopPlayersData().get(index);
    }

    public PlayerData getData(Player player, @NotNull SQLDateType type) {
        return this.data.get(type).getOnlinePlayersData().get(Pair.of(player.getName(), player.getUniqueId()));
    }

    public ImmutableMap<Pair<String, UUID>, PlayerData> dumpAllData(@NotNull SQLDateType type) {
        return ImmutableMap.copyOf(this.data.get(type).getTopPlayersData().asMap());
    }

    public void syncHeartbeat() {
        this.data.forEach((type, data) -> {
            try {
                data.syncHeartbeat();
            } catch (ExecutionException | InterruptedException e) {
                board.getPlugin().getLogger().severe("Failed to sync heartbeat for " + type.name() + " data!");
            }
        });
    }

    public void asyncHeartbeat() {
        if (this.asyncFutureData != null && this.asyncFutureData.isDone()) {
            try {
                Pair<Set<Pair<String, UUID>>, Map<Pair<String, UUID>, Pair<String, Map<String, String>>>> sortedData = asyncFutureData.get();
                this.board.getPlugin().getStorage().putPlayerDataForBoard(board, sortedData.getSecondValue());
                this.data.forEach((type, data) -> {
                    try {
                        data.asyncHeartbeat(this.board, sortedData.getFirstValue());
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

    public void update(boolean forceOffline) {
        if (this.asyncFutureData == null || this.asyncFutureData.isDone()) {
            this.asyncFutureData = new CompletableFuture<>();
            this.data.forEach((type, data) -> data.update());
            Map<Pair<String, UUID>, Pair<String, Map<String, String>>> playerData = new HashMap<>();
            Set<Pair<String, UUID>> onlinePlayers = new HashSet<>();
            // players_loop label
            players_loop:
            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID uuid = player.getUniqueId();
                String name = player.getName();
                onlinePlayers.add(Pair.of(name, uuid));
                String boardSorter = this.board.getBoardSettings().getSorter();
                String sorter = PlaceholderAPI.setPlaceholders(player, boardSorter);
                if (boardSorter.equals(sorter) || sorter.isEmpty())
                    continue;
                Map<String, String> playerTrackers = new HashMap<>();
                for (String entryKey : board.getBoardSettings().getTrackers().getKeys(false)) {
                    String entryValue = board.getBoardSettings().getTrackers().getString(entryKey, "%sorter%");
                    String value = PlaceholderAPI.setPlaceholders(player, entryValue).replace("%sorter%", sorter);
                    if (entryValue.equals(value) || value.isEmpty()) // if any of the tracker is empty or return the same value,
                        continue players_loop;                       // << continue to label (skip)
                    playerTrackers.put(entryKey, value);
                }
                playerData.put(Pair.of(name, uuid), Pair.of(sorter, playerTrackers));
            }
            if (forceOffline) {
                players_loop:
                for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                    UUID uuid = offlinePlayer.getUniqueId();
                    String name = offlinePlayer.getName();
                    String boardSorter = this.board.getBoardSettings().getSorter();
                    String sorter = PlaceholderAPI.setPlaceholders(offlinePlayer, boardSorter);
                    if (boardSorter.equals(sorter) || sorter.isEmpty())
                        continue;
                    Map<String, String> playerTrackers = new HashMap<>();
                    for (String entryKey : board.getBoardSettings().getTrackers().getKeys(false)) {
                        String entryValue = board.getBoardSettings().getTrackers().getString(entryKey, "%sorter%");
                        String value = PlaceholderAPI.setPlaceholders(offlinePlayer, entryValue).replace("%sorter%", sorter);
                        if (entryValue.equals(value) || value.isEmpty())
                            continue players_loop; // << continue to label
                        playerTrackers.put(entryKey, value);
                    }
                    playerData.put(Pair.of(name, uuid), Pair.of(sorter, playerTrackers));
                }
            }
            this.asyncFutureData.complete(Pair.of(onlinePlayers, playerData));
        }
    }
}

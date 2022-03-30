package ro.nico.leaderboard.storage.cache;

import com.google.common.collect.ImmutableMap;
import io.github.NicoNekoDev.SimpleTuples.Pair;
import io.github.NicoNekoDev.SimpleTuples.Triplet;
import lombok.AccessLevel;
import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.nico.leaderboard.api.Board;
import ro.nico.leaderboard.storage.SQLDateType;
import ro.nico.leaderboard.util.SyncAsyncOnlinePlayersData;
import ro.nico.leaderboard.util.SyncAsyncSortedData;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class BoardData {
    private final Board board;
    @Getter(AccessLevel.PROTECTED) private final IdentityHashMap<SQLDateType, SyncAsyncSortedData> sortedData;
    @Getter(AccessLevel.PROTECTED) private final IdentityHashMap<SQLDateType, SyncAsyncOnlinePlayersData> onlinePlayersData;

    private CompletableFuture<Map<Pair<String, UUID>, Pair<String, Map<String, String>>>> asyncFutureSortedData;
    private CompletableFuture<Set<Pair<String, UUID>>> asyncFutureOnlinePlayersData;

    private Map<Triplet<String, UUID, SQLDateType>, CompletableFuture<Triplet<String, Map<String, String>, Integer>>> immediateOnlinePlayersSyncFutureLists;
    private Map<Triplet<String, UUID, SQLDateType>, CompletableFuture<Pair<String, UUID>>> immediateOnlinePlayersAsyncFutureLists;

    public BoardData(@NotNull Board board) {
        this.board = board;
        this.sortedData = new IdentityHashMap<>(8) {
            {
                put(SQLDateType.ALLTIME, new SyncAsyncSortedData());
                if (board.getBoardSettings().isHourlyUpdated())
                    put(SQLDateType.HOURLY, new SyncAsyncSortedData());
                if (board.getBoardSettings().isDailyUpdated())
                    put(SQLDateType.DAILY, new SyncAsyncSortedData());
                if (board.getBoardSettings().isWeeklyUpdated())
                    put(SQLDateType.WEEKLY, new SyncAsyncSortedData());
                if (board.getBoardSettings().isMonthlyUpdated())
                    put(SQLDateType.MONTHLY, new SyncAsyncSortedData());
                if (board.getBoardSettings().isYearlyUpdated())
                    put(SQLDateType.YEARLY, new SyncAsyncSortedData());
            }
        };
        this.onlinePlayersData = new IdentityHashMap<>(8) {
            {
                put(SQLDateType.ALLTIME, new SyncAsyncOnlinePlayersData());
                if (board.getBoardSettings().isHourlyUpdated())
                    put(SQLDateType.HOURLY, new SyncAsyncOnlinePlayersData());
                if (board.getBoardSettings().isDailyUpdated())
                    put(SQLDateType.DAILY, new SyncAsyncOnlinePlayersData());
                if (board.getBoardSettings().isWeeklyUpdated())
                    put(SQLDateType.WEEKLY, new SyncAsyncOnlinePlayersData());
                if (board.getBoardSettings().isMonthlyUpdated())
                    put(SQLDateType.MONTHLY, new SyncAsyncOnlinePlayersData());
                if (board.getBoardSettings().isYearlyUpdated())
                    put(SQLDateType.YEARLY, new SyncAsyncOnlinePlayersData());
            }
        };
    }

    @Nullable
    public PlayerData getData(int index, @NotNull SQLDateType type) {
        return this.sortedData.get(type).getSortedData().get(index);
    }

    public PlayerData getData(Player player, @NotNull SQLDateType type) {
        return this.onlinePlayersData.get(type).getSortedData().get(Pair.of(player.getName(), player.getUniqueId()));
    }

    public ImmutableMap<Pair<String, UUID>, PlayerData> dumpAllData(@NotNull SQLDateType type) {
        return this.sortedData.get(type).dumpAllData();
    }

    public void updatePlayerDataImmediately(Player player) {
        for (SQLDateType type : this.sortedData.keySet()) {
            Triplet<String, UUID, SQLDateType> key = Triplet.of(player.getName(), player.getUniqueId(), type);
            immediateOnlinePlayersSyncFutureLists.computeIfAbsent(key, (k) -> new CompletableFuture<>());
            immediateOnlinePlayersAsyncFutureLists.computeIfAbsent(key, (k) -> {
                CompletableFuture<Pair<String, UUID>> future = new CompletableFuture<>();
                future.complete(key.toPair()); // <-- i love this
                return future;
            });
        }
    }

    public void syncHeartbeat() {
        Iterator<Map.Entry<Triplet<String, UUID, SQLDateType>, CompletableFuture<Triplet<String, Map<String, String>, Integer>>>> iterator = immediateOnlinePlayersSyncFutureLists.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Triplet<String, UUID, SQLDateType>, CompletableFuture<Triplet<String, Map<String, String>, Integer>>> entry = iterator.next();
            if (entry.getValue().isDone()) {
                try {
                    Triplet<String, UUID, SQLDateType> key = entry.getKey();
                    Triplet<String, Map<String, String>, Integer> triplet = entry.getValue().get();
                    this.onlinePlayersData.get(key.getThirdValue()).getSortedData().put(key.toPair(), new PlayerData(triplet.getFirstValue(), triplet.getSecondValue(), triplet.getThirdValue()));
                    iterator.remove();
                } catch (InterruptedException | ExecutionException e) {
                    board.getPlugin().getLogger().log(Level.SEVERE, "Failed to sync player data", e);
                }
            }
        }
        this.sortedData.forEach((type, data) -> {
            try {
                data.syncHeartbeat();
            } catch (ExecutionException | InterruptedException e) {
                board.getPlugin().getLogger().severe("Failed to sync heartbeat for " + type.name() + " data!");
            }
        });
        this.onlinePlayersData.forEach((type, data) -> {
            try {
                data.syncHeartbeat();
            } catch (ExecutionException | InterruptedException e) {
                board.getPlugin().getLogger().severe("Failed to sync heartbeat for " + type.name() + " data!");
            }
        });
    }

    public void asyncHeartbeat() {
        Iterator<Map.Entry<Triplet<String, UUID, SQLDateType>, CompletableFuture<Pair<String, UUID>>>> iterator = immediateOnlinePlayersAsyncFutureLists.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Triplet<String, UUID, SQLDateType>, CompletableFuture<Pair<String, UUID>>> entry = iterator.next();
            if (entry.getValue().isDone()) {
                try {
                    Triplet<String, UUID, SQLDateType> key = entry.getKey();
                    Pair<String, UUID> pair = entry.getValue().get();
                    Triplet<String, Map<String, String>, Integer> data = this.board.getPlugin().getStorage().getOnlinePlayerDataImmediately(pair, board, key.getThirdValue());
                    CompletableFuture<Triplet<String, Map<String, String>, Integer>> future = this.immediateOnlinePlayersSyncFutureLists.remove(key);
                    if (future != null && !future.isDone())
                        future.complete(data);
                    iterator.remove();
                } catch (InterruptedException | ExecutionException | SQLException e) {
                    board.getPlugin().getLogger().log(Level.SEVERE, "Failed to sync player data", e);
                }
            }
        }
        if (this.asyncFutureSortedData != null && this.asyncFutureSortedData.isDone()) {
            try {
                Map<Pair<String, UUID>, Pair<String, Map<String, String>>> sortedData = asyncFutureSortedData.get();
                this.board.getPlugin().getStorage().putPlayerDataForBoard(board, sortedData);
                this.sortedData.forEach((type, data) -> {
                    try {
                        data.asyncHeartbeat(this.board);
                    } catch (SQLException e) {
                        board.getPlugin().getLogger().log(Level.SEVERE, "Failed to async heartbeat for " + type.name() + " data!", e);
                    }
                });
                this.asyncFutureSortedData = null;
            } catch (InterruptedException | ExecutionException | SQLException e) {
                board.getPlugin().getLogger().log(Level.SEVERE, "Error while syncing data", e);
            }
        }
        if (this.asyncFutureOnlinePlayersData != null && this.asyncFutureOnlinePlayersData.isDone()) {
            try {
                Set<Pair<String, UUID>> onlinePlayersData = asyncFutureOnlinePlayersData.get();
                this.onlinePlayersData.forEach((type, data) -> {
                    try {
                        data.asyncHeartbeat(this.board, onlinePlayersData);
                    } catch (SQLException e) {
                        board.getPlugin().getLogger().log(Level.SEVERE, "Failed to async heartbeat for " + type.name() + " data!", e);
                    }
                });
                this.asyncFutureOnlinePlayersData = null;
            } catch (InterruptedException | ExecutionException e) {
                board.getPlugin().getLogger().log(Level.SEVERE, "Error while syncing data", e);
            }
        }
    }

    public void update() {
        if (this.asyncFutureSortedData == null || this.asyncFutureSortedData.isDone()) {
            this.asyncFutureSortedData = new CompletableFuture<>();
            this.sortedData.forEach((type, data) -> data.update());
            Map<Pair<String, UUID>, Pair<String, Map<String, String>>> playerData = new HashMap<>();
            // players_loop label
            players_loop:
            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID uuid = player.getUniqueId();
                String name = player.getName();
                if (board.hasPlayerExempt(name) || board.hasPlayerExempt(uuid))
                    continue;
                Map<String, String> playerTrackers = new HashMap<>();
                for (Map.Entry<String, Object> entry : board.getTrackers().entrySet()) {
                    String entryValue = String.valueOf(entry.getValue());
                    String value = PlaceholderAPI.setPlaceholders(player, entryValue);
                    if (entryValue.equals(value)) continue players_loop; // << continue to label
                    playerTrackers.put(entry.getKey(), value);
                }
                String boardSorter = this.board.getBoardSettings().getSorter();
                String sorter = PlaceholderAPI.setPlaceholders(player, boardSorter);
                if (boardSorter.equals(sorter)) continue; // << continue to label
                playerData.put(Pair.of(name, uuid), Pair.of(sorter, playerTrackers));
            }
            this.asyncFutureSortedData.complete(playerData);
        }
        if (this.asyncFutureOnlinePlayersData == null || this.asyncFutureOnlinePlayersData.isDone()) {
            this.asyncFutureOnlinePlayersData = new CompletableFuture<>();
            this.asyncFutureOnlinePlayersData.complete(Bukkit.getOnlinePlayers().stream().map(player -> Pair.of(player.getName(), player.getUniqueId())).collect(Collectors.toSet()));
        }
    }
}

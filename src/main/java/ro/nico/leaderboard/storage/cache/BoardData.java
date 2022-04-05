package ro.nico.leaderboard.storage.cache;

import com.google.common.collect.ImmutableMap;
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
        this.data.put(SQLDateType.ALLTIME, new RankedMapList(this.board));
    }

    public void unload() {
        if (this.asyncFutureData != null && this.asyncFutureData.cancel(false))
            this.asyncFutureData = null;
        this.data.values().forEach(RankedMapList::unload);
    }

    @Nullable
    public PlayerData getData(int rank, @NotNull SQLDateType type) {
        if (!this.getData().containsKey(type))
            return null;
        return this.data.get(type).getByRank(rank);
    }

    public PlayerData getData(Player player, @NotNull SQLDateType type) {
        return this.data.get(type).getByKey(new PlayerId(player.getName(), player.getUniqueId()));
    }

    public ImmutableMap<PlayerId, PlayerData> dumpAllData(@NotNull SQLDateType type) {
        return ImmutableMap.copyOf(this.data.get(type).asMap());
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

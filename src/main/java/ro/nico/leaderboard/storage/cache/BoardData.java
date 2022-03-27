package ro.nico.leaderboard.storage.cache;

import io.github.NicoNekoDev.SimpleTuples.Pair;
import io.github.NicoNekoDev.SimpleTuples.Triplet;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.nico.leaderboard.api.Board;
import ro.nico.leaderboard.storage.SQLDateType;
import ro.nico.leaderboard.util.LinkedMapList;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

public class BoardData {
    private final Board board;
    private final LinkedMapList<Pair<String, UUID>, PlayerData> sortedDataAllTime = new LinkedMapList<>();
    private final LinkedMapList<Pair<String, UUID>, PlayerData> sortedDataHourly = new LinkedMapList<>();
    private final LinkedMapList<Pair<String, UUID>, PlayerData> sortedDataDaily = new LinkedMapList<>();
    private final LinkedMapList<Pair<String, UUID>, PlayerData> sortedDataWeekly = new LinkedMapList<>();
    private final LinkedMapList<Pair<String, UUID>, PlayerData> sortedDataMonthly = new LinkedMapList<>();
    private final LinkedMapList<Pair<String, UUID>, PlayerData> sortedDataYearly = new LinkedMapList<>();

    private CompletableFuture<LinkedList<Triplet<Pair<String, UUID>, String, Map<String, String>>>> syncFutureAllTime;
    private CompletableFuture<LinkedList<Triplet<Pair<String, UUID>, String, Map<String, String>>>> syncFutureHourly;
    private CompletableFuture<LinkedList<Triplet<Pair<String, UUID>, String, Map<String, String>>>> syncFutureDaily;
    private CompletableFuture<LinkedList<Triplet<Pair<String, UUID>, String, Map<String, String>>>> syncFutureWeekly;
    private CompletableFuture<LinkedList<Triplet<Pair<String, UUID>, String, Map<String, String>>>> syncFutureMonthly;
    private CompletableFuture<LinkedList<Triplet<Pair<String, UUID>, String, Map<String, String>>>> syncFutureYearly;

    private CompletableFuture<Map<Pair<String, UUID>, Pair<String, Map<String, String>>>> asyncFuture;

    public BoardData(@NotNull Board board) {
        this.board = board;
    }

    @Nullable
    public PlayerData getData(int index, @NotNull SQLDateType type) {
        return switch (type) {
            case ALLTIME -> sortedDataAllTime.get(index);
            case HOURLY -> sortedDataHourly.get(index);
            case DAILY -> sortedDataDaily.get(index);
            case WEEKLY -> sortedDataWeekly.get(index);
            case MONTHLY -> sortedDataMonthly.get(index);
            case YEARLY -> sortedDataYearly.get(index);
        };
    }

    public int getIndexOfPlayer(Player player, @NotNull SQLDateType type) {
        Pair<String, UUID> key = Pair.of(player.getName(), player.getUniqueId());
        return switch (type) {
            case ALLTIME -> sortedDataAllTime.indexOf(key);
            case HOURLY -> sortedDataHourly.indexOf(key);
            case DAILY -> sortedDataDaily.indexOf(key);
            case WEEKLY -> sortedDataWeekly.indexOf(key);
            case MONTHLY -> sortedDataMonthly.indexOf(key);
            case YEARLY -> sortedDataYearly.indexOf(key);
        };
    }

    public void syncHeartbeat() {
        if (this.syncFutureAllTime != null && this.syncFutureAllTime.isDone()) {
            try {
                LinkedList<Triplet<Pair<String, UUID>, String, Map<String, String>>> sortedData = syncFutureAllTime.get();
                this.sortedDataAllTime.clear();
                for (Triplet<Pair<String, UUID>, String, Map<String, String>> triplet : sortedData) {
                    this.sortedDataAllTime.put(triplet.getFirstValue(), new PlayerData(triplet.getSecondValue(), triplet.getThirdValue()));
                }
                this.syncFutureAllTime = null;
            } catch (InterruptedException | ExecutionException e) {
                board.getPlugin().getLogger().log(Level.SEVERE, "Error while syncing data", e);
            }
        }
        if (this.syncFutureHourly != null && this.syncFutureHourly.isDone() && this.board.isHourlyUpdateEnabled()) {
            try {
                LinkedList<Triplet<Pair<String, UUID>, String, Map<String, String>>> sortedData = syncFutureHourly.get();
                this.sortedDataHourly.clear();
                for (Triplet<Pair<String, UUID>, String, Map<String, String>> triplet : sortedData) {
                    this.sortedDataHourly.put(triplet.getFirstValue(), new PlayerData(triplet.getSecondValue(), triplet.getThirdValue()));
                }
                this.syncFutureHourly = null;
            } catch (InterruptedException | ExecutionException e) {
                board.getPlugin().getLogger().log(Level.SEVERE, "Error while syncing data", e);
            }
        }
        if (this.syncFutureDaily != null && this.syncFutureDaily.isDone() && this.board.isDailyUpdateEnabled()) {
            try {
                LinkedList<Triplet<Pair<String, UUID>, String, Map<String, String>>> sortedData = syncFutureDaily.get();
                this.sortedDataDaily.clear();
                for (Triplet<Pair<String, UUID>, String, Map<String, String>> triplet : sortedData) {
                    this.sortedDataDaily.put(triplet.getFirstValue(), new PlayerData(triplet.getSecondValue(), triplet.getThirdValue()));
                }
                this.syncFutureDaily = null;
            } catch (InterruptedException | ExecutionException e) {
                board.getPlugin().getLogger().log(Level.SEVERE, "Error while syncing data", e);
            }
        }
        if (this.syncFutureWeekly != null && this.syncFutureWeekly.isDone() && this.board.isWeeklyUpdateEnabled()) {
            try {
                LinkedList<Triplet<Pair<String, UUID>, String, Map<String, String>>> sortedData = syncFutureWeekly.get();
                this.sortedDataWeekly.clear();
                for (Triplet<Pair<String, UUID>, String, Map<String, String>> triplet : sortedData) {
                    this.sortedDataWeekly.put(triplet.getFirstValue(), new PlayerData(triplet.getSecondValue(), triplet.getThirdValue()));
                }
                this.syncFutureWeekly = null;
            } catch (InterruptedException | ExecutionException e) {
                board.getPlugin().getLogger().log(Level.SEVERE, "Error while syncing data", e);
            }
        }
        if (this.syncFutureMonthly != null && this.syncFutureMonthly.isDone() && this.board.isMonthlyUpdateEnabled()) {
            try {
                LinkedList<Triplet<Pair<String, UUID>, String, Map<String, String>>> sortedData = syncFutureMonthly.get();
                this.sortedDataMonthly.clear();
                for (Triplet<Pair<String, UUID>, String, Map<String, String>> triplet : sortedData) {
                    this.sortedDataMonthly.put(triplet.getFirstValue(), new PlayerData(triplet.getSecondValue(), triplet.getThirdValue()));
                }
                this.syncFutureMonthly = null;
            } catch (InterruptedException | ExecutionException e) {
                board.getPlugin().getLogger().log(Level.SEVERE, "Error while syncing data", e);
            }
        }
        if (this.syncFutureYearly != null && this.syncFutureYearly.isDone() && this.board.isYearlyUpdateEnabled()) {
            try {
                LinkedList<Triplet<Pair<String, UUID>, String, Map<String, String>>> sortedData = syncFutureYearly.get();
                this.sortedDataYearly.clear();
                for (Triplet<Pair<String, UUID>, String, Map<String, String>> triplet : sortedData) {
                    this.sortedDataYearly.put(triplet.getFirstValue(), new PlayerData(triplet.getSecondValue(), triplet.getThirdValue()));
                }
                this.syncFutureYearly = null;
            } catch (InterruptedException | ExecutionException e) {
                board.getPlugin().getLogger().log(Level.SEVERE, "Error while syncing data", e);
            }
        }
    }

    public void asyncHeartbeat() {
        if (this.asyncFuture != null && this.asyncFuture.isDone()) {
            try {
                Map<Pair<String, UUID>, Pair<String, Map<String, String>>> sortedData = asyncFuture.get();
                this.board.getPlugin().getStorage().putPlayerDataForBoard(board, sortedData);
                if (this.syncFutureAllTime != null)
                    this.syncFutureAllTime.complete(this.board.getPlugin().getStorage().getPlayerDataForBoard(board, SQLDateType.ALLTIME));
                if (this.syncFutureHourly != null && this.board.isHourlyUpdateEnabled())
                    this.syncFutureHourly.complete(this.board.getPlugin().getStorage().getPlayerDataForBoard(board, SQLDateType.HOURLY));
                if (this.syncFutureDaily != null && this.board.isDailyUpdateEnabled())
                    this.syncFutureDaily.complete(this.board.getPlugin().getStorage().getPlayerDataForBoard(board, SQLDateType.DAILY));
                if (this.syncFutureWeekly != null && this.board.isWeeklyUpdateEnabled())
                    this.syncFutureWeekly.complete(this.board.getPlugin().getStorage().getPlayerDataForBoard(board, SQLDateType.WEEKLY));
                if (this.syncFutureMonthly != null && this.board.isMonthlyUpdateEnabled())
                    this.syncFutureMonthly.complete(this.board.getPlugin().getStorage().getPlayerDataForBoard(board, SQLDateType.MONTHLY));
                if (this.syncFutureYearly != null && this.board.isYearlyUpdateEnabled())
                    this.syncFutureYearly.complete(this.board.getPlugin().getStorage().getPlayerDataForBoard(board, SQLDateType.YEARLY));
                this.asyncFuture = null;
            } catch (InterruptedException | ExecutionException | SQLException e) {
                board.getPlugin().getLogger().log(Level.SEVERE, "Error while syncing data", e);
            }
        }
    }

    public void update() {
        if (this.asyncFuture == null || this.asyncFuture.isDone()) {
            this.asyncFuture = new CompletableFuture<>();
            this.syncFutureAllTime = new CompletableFuture<>();
            if (this.board.isHourlyUpdateEnabled())
                this.syncFutureHourly = new CompletableFuture<>();
            if (this.board.isDailyUpdateEnabled())
                this.syncFutureDaily = new CompletableFuture<>();
            if (this.board.isWeeklyUpdateEnabled())
                this.syncFutureWeekly = new CompletableFuture<>();
            if (this.board.isMonthlyUpdateEnabled())
                this.syncFutureMonthly = new CompletableFuture<>();
            if (this.board.isYearlyUpdateEnabled())
                this.syncFutureYearly = new CompletableFuture<>();
            Map<Pair<String, UUID>, Pair<String, Map<String, String>>> playerData = new HashMap<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (board.hasPlayerExempt(player.getName()) || board.hasPlayerExempt(player.getUniqueId()))
                    continue;
                Map<String, String> playerTrackers = new HashMap<>();
                for (String tracker : board.getTrackers().getKeys(false)) {
                    String trackerValue = PlaceholderAPI.setPlaceholders(player, board.getTrackers().getString(tracker, "%player_name%"));
                    playerTrackers.put(tracker, trackerValue);
                }
                playerData.put(new Pair<>(player.getName(), player.getUniqueId()), new Pair<>(
                        PlaceholderAPI.setPlaceholders(player, this.board.getSorter()),
                        playerTrackers
                ));
            }
            this.asyncFuture.complete(playerData);
        }
    }
}

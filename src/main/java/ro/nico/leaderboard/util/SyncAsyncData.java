package ro.nico.leaderboard.util;

import io.github.NicoNekoDev.SimpleTuples.Pair;
import io.github.NicoNekoDev.SimpleTuples.Quartet;
import io.github.NicoNekoDev.SimpleTuples.Triplet;
import lombok.Getter;
import ro.nico.leaderboard.api.Board;
import ro.nico.leaderboard.storage.SQLDateType;
import ro.nico.leaderboard.storage.cache.PlayerData;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class SyncAsyncData {
    @Getter private final LinkedMapList<Pair<String, UUID>, PlayerData> topPlayersData = new LinkedMapList<>();
    @Getter private final LinkedMapList<Pair<String, UUID>, PlayerData> onlinePlayersData = new LinkedMapList<>();
    @Getter private CompletableFuture<Pair<Map<Pair<String, UUID>, Triplet<String, Map<String, String>, Integer>>, LinkedList<Quartet<Pair<String, UUID>, String, Map<String, String>, Integer>>>> syncFuture;

    public void syncHeartbeat() throws ExecutionException, InterruptedException {
        if (this.syncFuture != null && this.syncFuture.isDone()) {
            Pair<Map<Pair<String, UUID>, Triplet<String, Map<String, String>, Integer>>, LinkedList<Quartet<Pair<String, UUID>, String, Map<String, String>, Integer>>> pairData = syncFuture.get();
            this.topPlayersData.clear();
            this.onlinePlayersData.clear();
            for (Quartet<Pair<String, UUID>, String, Map<String, String>, Integer> quartet : pairData.getSecondValue()) {
                this.topPlayersData.put(quartet.getFirstValue(), new PlayerData(quartet.getSecondValue(), quartet.getThirdValue(), quartet.getForthValue())); // << 'forth' means 'fourth' but it's a typo
            }
            for (Map.Entry<Pair<String, UUID>, Triplet<String, Map<String, String>, Integer>> entry : pairData.getFirstValue().entrySet()) {
                this.onlinePlayersData.put(entry.getKey(), new PlayerData(entry.getValue().getFirstValue(), entry.getValue().getSecondValue(), entry.getValue().getThirdValue()));
            }
            this.syncFuture = null;
        }
    }

    public void asyncHeartbeat(Board board, Set<Pair<String, UUID>> onlinePlayers) throws SQLException {
        if (this.syncFuture != null)
            this.syncFuture.complete(board.getPlugin().getStorage().getDataForBoard(onlinePlayers, board, SQLDateType.ALLTIME));
    }

    public void update() {
        this.syncFuture = new CompletableFuture<>();
    }

    public void unload() {
        if (this.syncFuture != null && this.syncFuture.cancel(false))
            this.syncFuture = null;
        this.topPlayersData.clear();
        this.onlinePlayersData.clear();
    }
}

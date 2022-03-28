package ro.nico.leaderboard.util;

import io.github.NicoNekoDev.SimpleTuples.Pair;
import io.github.NicoNekoDev.SimpleTuples.Triplet;
import ro.nico.leaderboard.api.Board;
import ro.nico.leaderboard.storage.SQLDateType;
import ro.nico.leaderboard.storage.cache.PlayerData;

import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class SyncAsyncOnlinePlayersData {
    private final LinkedMapList<Pair<String, UUID>, PlayerData> playerData = new LinkedMapList<>();
    private CompletableFuture<Map<Pair<String, UUID>, Triplet<String, Map<String, String>, Integer>>> syncFuture;

    public void syncHeartbeat() throws ExecutionException, InterruptedException {
        if (this.syncFuture != null && this.syncFuture.isDone()) {
            Map<Pair<String, UUID>, Triplet<String, Map<String, String>, Integer>> data = syncFuture.get();
            this.playerData.clear();
            for (Map.Entry<Pair<String, UUID>, Triplet<String, Map<String, String>, Integer>> entry : data.entrySet()) {
                this.playerData.put(entry.getKey(), new PlayerData(entry.getValue().getFirstValue(), entry.getValue().getSecondValue(), entry.getValue().getThirdValue()));
            }
            this.syncFuture = null;
        }
    }

    public void asyncHeartbeat(Board board, Set<Pair<String, UUID>> onlinePlayers) throws SQLException {
        if (this.syncFuture != null)
            this.syncFuture.complete(board.getPlugin().getStorage().getOnlinePlayersDataForBoard(onlinePlayers, board, SQLDateType.ALLTIME));
    }

    public void update() {
        this.syncFuture = new CompletableFuture<>();
    }

    public LinkedMapList<Pair<String, UUID>, PlayerData> getSortedData() {
        return this.playerData;
    }

    public CompletableFuture<Map<Pair<String, UUID>, Triplet<String, Map<String, String>, Integer>>> getSyncFuture() {
        return this.syncFuture;
    }
}

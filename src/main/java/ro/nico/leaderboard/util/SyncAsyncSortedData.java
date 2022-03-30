package ro.nico.leaderboard.util;

import com.google.common.collect.ImmutableMap;
import io.github.NicoNekoDev.SimpleTuples.Pair;
import io.github.NicoNekoDev.SimpleTuples.Quartet;
import lombok.Getter;
import ro.nico.leaderboard.api.Board;
import ro.nico.leaderboard.storage.SQLDateType;
import ro.nico.leaderboard.storage.cache.PlayerData;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class SyncAsyncSortedData {
    @Getter private final LinkedMapList<Pair<String, UUID>, PlayerData> sortedData = new LinkedMapList<>();
    @Getter private CompletableFuture<LinkedList<Quartet<Pair<String, UUID>, String, Map<String, String>, Integer>>> syncFuture;

    public void syncHeartbeat() throws ExecutionException, InterruptedException {
        if (this.syncFuture != null && this.syncFuture.isDone()) {
            LinkedList<Quartet<Pair<String, UUID>, String, Map<String, String>, Integer>> sortedData = syncFuture.get();
            this.sortedData.clear();
            for (Quartet<Pair<String, UUID>, String, Map<String, String>, Integer> quartet : sortedData) {
                this.sortedData.put(quartet.getFirstValue(), new PlayerData(quartet.getSecondValue(), quartet.getThirdValue(), quartet.getForthValue())); // << 'forth' means 'fourth' but it's a typo
            }
            this.syncFuture = null;
        }
    }

    public void asyncHeartbeat(Board board) throws SQLException {
        if (this.syncFuture != null)
            this.syncFuture.complete(board.getPlugin().getStorage().getPlayersDataForBoard(board, SQLDateType.ALLTIME));
    }

    public void update() {
        this.syncFuture = new CompletableFuture<>();
    }

    public ImmutableMap<Pair<String, UUID>, PlayerData> dumpAllData() {
        return ImmutableMap.copyOf(this.sortedData.asMap());
    }
}

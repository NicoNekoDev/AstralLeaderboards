package ro.nico.leaderboard.storage.types;

import io.github.NicoNekoDev.SimpleTuples.Pair;
import ro.nico.leaderboard.AstralLeaderboardsPlugin;
import ro.nico.leaderboard.api.Board;
import ro.nico.leaderboard.data.PlayerData;
import ro.nico.leaderboard.data.PlayerId;
import ro.nico.leaderboard.storage.SQLDateType;
import ro.nico.leaderboard.storage.settings.StorageSettings;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public abstract class Storage {
    protected final AstralLeaderboardsPlugin plugin;

    public Storage(AstralLeaderboardsPlugin plugin) {
        this.plugin = plugin;
    }

    public abstract void load(StorageSettings settings) throws SQLException;

    public abstract void unload() throws SQLException;

    public abstract void putDataForBoard(Board board, Map<PlayerId, PlayerData> data) throws SQLException;

    public abstract void getDataForBoard(Pair<ConcurrentMap<Integer, PlayerId>, ConcurrentMap<PlayerId, PlayerData>> pair, Board board, SQLDateType dateType) throws SQLException;
}

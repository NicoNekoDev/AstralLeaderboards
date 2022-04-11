package ro.nico.leaderboard.storage;

import ro.nico.leaderboard.AstralLeaderboardsPlugin;
import ro.nico.leaderboard.api.Board;
import ro.nico.leaderboard.data.PlayerData;
import ro.nico.leaderboard.data.PlayerId;
import ro.nico.leaderboard.storage.settings.StorageSettings;
import ro.nico.leaderboard.storage.types.MySQLStorage;
import ro.nico.leaderboard.storage.types.SQLiteStorage;
import ro.nico.leaderboard.storage.types.Storage;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class StorageConfiguration extends Storage {
    private Storage storage;

    public StorageConfiguration(AstralLeaderboardsPlugin plugin) {
        super(plugin);
    }

    public void load(StorageSettings settings) throws SQLException {
        if (storage != null)
            storage.unload();
        if (settings.isUsingMySQL())
            storage = new MySQLStorage(plugin);
        else
            storage = new SQLiteStorage(plugin);
        storage.load(settings);
    }

    @Override
    public void unload() throws SQLException {
        if (storage != null)
            storage.unload();
    }

    @Override
    public void putDataForBoard(Board board, Map<PlayerId, PlayerData> data) throws SQLException {
        if (storage != null)
            storage.putDataForBoard(board, data);
    }

    @Override
    public void getDataForBoard(ConcurrentMap<Integer, PlayerId> rankMap, ConcurrentMap<PlayerId, PlayerData> dataMap, Board board, SQLDateType dateType) throws SQLException {
        if (storage != null)
            storage.getDataForBoard(rankMap, dataMap, board, dateType);
    }
}

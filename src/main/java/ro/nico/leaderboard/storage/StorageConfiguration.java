package ro.nico.leaderboard.storage;

import io.github.NicoNekoDev.SimpleTuples.Pair;
import io.github.NicoNekoDev.SimpleTuples.Quartet;
import io.github.NicoNekoDev.SimpleTuples.Triplet;
import ro.nico.leaderboard.AstralLeaderboardsPlugin;
import ro.nico.leaderboard.api.Board;
import ro.nico.leaderboard.storage.settings.StorageSettings;
import ro.nico.leaderboard.storage.types.MySQLStorage;
import ro.nico.leaderboard.storage.types.SQLiteStorage;
import ro.nico.leaderboard.storage.types.Storage;

import java.sql.SQLException;
import java.util.*;

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
    public void putPlayerDataForBoard(Board board, Map<Pair<String, UUID>, Pair<String, Map<String, String>>> sortedData) throws SQLException {
        if (this.storage != null)
            this.storage.putPlayerDataForBoard(board, sortedData);
    }

    @Override
    public Pair<Map<Pair<String, UUID>, Triplet<String, Map<String, String>, Integer>>, LinkedList<Quartet<Pair<String, UUID>, String, Map<String, String>, Integer>>> getDataForBoard(Set<Pair<String, UUID>> players, Board board, SQLDateType dateType) throws SQLException {
        if (this.storage != null)
            return this.storage.getDataForBoard(players, board, dateType);
        return Pair.of(new HashMap<>(), new LinkedList<>());
    }


    @Override
    public Triplet<String, Map<String, String>, Integer> getOnlinePlayerDataImmediately(Pair<String, UUID> player, Board board, SQLDateType dateType) throws SQLException {
        if (this.storage != null)
            return this.storage.getOnlinePlayerDataImmediately(player, board, dateType);
        return Triplet.of("", new HashMap<>(), -1);
    }

}

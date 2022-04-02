package ro.nico.leaderboard.storage;

import io.github.NicoNekoDev.SimpleTuples.Pair;
import io.github.NicoNekoDev.SimpleTuples.Quartet;
import io.github.NicoNekoDev.SimpleTuples.Triplet;
import ro.nico.leaderboard.AstralLeaderboardsPlugin;
import ro.nico.leaderboard.api.Board;
import ro.nico.leaderboard.storage.types.MySQLStorage;
import ro.nico.leaderboard.storage.types.SQLiteStorage;
import ro.nico.leaderboard.storage.types.Storage;
import ro.nico.leaderboard.settings.PluginSettings;

import java.sql.SQLException;
import java.util.*;

public class StorageConfiguration extends Storage {
    private Storage storage;

    public StorageConfiguration(AstralLeaderboardsPlugin plugin) {
        super(plugin);
    }

    public void load(PluginSettings.StorageSettings settings) throws SQLException {
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
    public LinkedList<Quartet<Pair<String, UUID>, String, Map<String, String>, Integer>> getPlayersDataForBoard(Board board, SQLDateType type) throws SQLException {
        if (this.storage != null)
            return this.storage.getPlayersDataForBoard(board, type);
        return new LinkedList<>();
    }

    @Override
    public Map<Pair<String, UUID>, Triplet<String, Map<String, String>, Integer>> getOnlinePlayersDataForBoard(Set<Pair<String, UUID>> players, Board board, SQLDateType dateType) throws SQLException {
        if (this.storage != null)
            return this.storage.getOnlinePlayersDataForBoard(players, board, dateType);
        return new HashMap<>();
    }

    @Override
    public Triplet<String, Map<String, String>, Integer> getOnlinePlayerDataImmediately(Pair<String, UUID> player, Board board, SQLDateType dateType) throws SQLException {
        if (this.storage != null)
            return this.storage.getOnlinePlayerDataImmediately(player, board, dateType);
        return Triplet.of("", new HashMap<>(), -1);
    }

}

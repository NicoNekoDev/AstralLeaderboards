package ro.nico.leaderboard.storage;

import io.github.NicoNekoDev.SimpleTuples.Pair;
import io.github.NicoNekoDev.SimpleTuples.Triplet;
import org.bukkit.configuration.file.YamlConfiguration;
import ro.nico.leaderboard.AstralLeaderboardsPlugin;
import ro.nico.leaderboard.api.Board;
import ro.nico.leaderboard.storage.types.MySQLStorage;
import ro.nico.leaderboard.storage.types.SQLiteStorage;
import ro.nico.leaderboard.storage.types.Storage;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

public class StorageConfiguration extends Storage {
    private Storage storage;

    public StorageConfiguration(AstralLeaderboardsPlugin plugin) {
        super(plugin);
    }

    public void load(YamlConfiguration config) throws SQLException {
        if (storage != null)
            storage.unload();
        if (config.getBoolean("use-mysql", false))
            storage = new MySQLStorage(plugin);
        else
            storage = new SQLiteStorage(plugin);
        storage.load(config);
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
    public LinkedList<Triplet<Pair<String, UUID>, String, Map<String, String>>> getPlayerDataForBoard(Board board, SQLDateType type) throws SQLException {
        if (this.storage != null)
            return this.storage.getPlayerDataForBoard(board, type);
        return new LinkedList<>();
    }
}

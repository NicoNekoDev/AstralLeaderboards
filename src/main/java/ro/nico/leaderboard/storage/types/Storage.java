package ro.nico.leaderboard.storage.types;

import io.github.NicoNekoDev.SimpleTuples.Pair;
import io.github.NicoNekoDev.SimpleTuples.Triplet;
import org.bukkit.configuration.file.YamlConfiguration;
import ro.nico.leaderboard.AstralLeaderboardsPlugin;
import ro.nico.leaderboard.api.Board;
import ro.nico.leaderboard.storage.SQLDateType;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

public abstract class Storage {
    protected final AstralLeaderboardsPlugin plugin;

    public Storage(AstralLeaderboardsPlugin plugin) {
        this.plugin = plugin;
    }

    public abstract void load(YamlConfiguration settings) throws SQLException;

    public abstract void unload() throws SQLException;

    public abstract void putPlayerDataForBoard(Board board, Map<Pair<String, UUID>, Pair<String, Map<String, String>>> sortedData) throws SQLException;

    public abstract LinkedList<Triplet<Pair<String, UUID>, String, Map<String, String>>> getPlayerDataForBoard(Board board, SQLDateType type) throws SQLException;
}

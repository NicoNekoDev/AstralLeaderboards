package ro.nico.leaderboard.storage.types;

import io.github.NicoNekoDev.SimpleTuples.Pair;
import io.github.NicoNekoDev.SimpleTuples.Quartet;
import io.github.NicoNekoDev.SimpleTuples.Triplet;
import ro.nico.leaderboard.AstralLeaderboardsPlugin;
import ro.nico.leaderboard.api.Board;
import ro.nico.leaderboard.storage.SQLDateType;
import ro.nico.leaderboard.settings.PluginSettings;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public abstract class Storage {
    protected final AstralLeaderboardsPlugin plugin;

    public Storage(AstralLeaderboardsPlugin plugin) {
        this.plugin = plugin;
    }

    public abstract void load(PluginSettings settings) throws SQLException;

    public abstract void unload() throws SQLException;

    public abstract void putPlayerDataForBoard(Board board, Map<Pair<String, UUID>, Pair<String, Map<String, String>>> sortedData) throws SQLException;

    public abstract LinkedList<Quartet<Pair<String, UUID>, String, Map<String, String>, Integer>> getPlayersDataForBoard(Board board, SQLDateType type) throws SQLException;

    public abstract Map<Pair<String, UUID>, Triplet<String, Map<String, String>, Integer>> getOnlinePlayersDataForBoard(Set<Pair<String, UUID>> players, Board board, SQLDateType dateType) throws SQLException;

    public abstract Triplet<String, Map<String, String>, Integer> getOnlinePlayerDataImmediately(Pair<String, UUID> player, Board board, SQLDateType dateType) throws SQLException;
}

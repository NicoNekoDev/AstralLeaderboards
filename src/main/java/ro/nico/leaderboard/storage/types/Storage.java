package ro.nico.leaderboard.storage.types;

import io.github.NicoNekoDev.SimpleTuples.Pair;
import io.github.NicoNekoDev.SimpleTuples.Quartet;
import io.github.NicoNekoDev.SimpleTuples.Triplet;
import ro.nico.leaderboard.AstralLeaderboardsPlugin;
import ro.nico.leaderboard.api.Board;
import ro.nico.leaderboard.storage.SQLDateType;
import ro.nico.leaderboard.storage.settings.StorageSettings;

import java.sql.SQLException;
import java.util.*;

public abstract class Storage {
    protected final AstralLeaderboardsPlugin plugin;

    public Storage(AstralLeaderboardsPlugin plugin) {
        this.plugin = plugin;
    }

    public abstract void load(StorageSettings settings) throws SQLException;

    public abstract void unload() throws SQLException;

    public abstract void putPlayerDataForBoard(Board board, Map<Pair<String, UUID>, Pair<String, Map<String, String>>> sortedData) throws SQLException;

    public abstract Pair<Map<Pair<String, UUID>, Triplet<String, Map<String, String>, Integer>>,
            LinkedList<Quartet<Pair<String, UUID>, String, Map<String, String>, Integer>>> getDataForBoard
            (Set<Pair<String, UUID>> players, Board board, SQLDateType dateType) throws SQLException;

    public abstract Triplet<String, Map<String, String>, Integer> getOnlinePlayerDataImmediately(Pair<String, UUID> player, Board board, SQLDateType dateType) throws SQLException;
}

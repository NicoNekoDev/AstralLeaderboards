package ro.nico.leaderboard.storage.types;

import io.github.NicoNekoDev.SimpleTuples.Pair;
import io.github.NicoNekoDev.SimpleTuples.Quartet;
import io.github.NicoNekoDev.SimpleTuples.Triplet;
import org.bukkit.Bukkit;
import ro.nico.leaderboard.AstralLeaderboardsPlugin;
import ro.nico.leaderboard.api.Board;
import ro.nico.leaderboard.storage.SQLDateType;
import ro.nico.leaderboard.storage.settings.StorageSettings;
import ro.nico.leaderboard.util.GsonUtil;

import java.io.File;
import java.sql.*;
import java.util.*;

public class SQLiteStorage extends Storage {
    private Connection connection;

    public SQLiteStorage(AstralLeaderboardsPlugin plugin) {
        super(plugin);
    }

    public void load(StorageSettings settings) throws SQLException {
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + super.plugin.getDataFolder() + File.separator + settings.getSQLiteSettings().getFileName());
        try (PreparedStatement statement = this.connection.prepareStatement("""
                CREATE TABLE IF NOT EXISTS `leaderboard` (
                board_id VARCHAR(255) NOT NULL,
                player_name VARCHAR(255) NOT NULL,
                player_uuid VARCHAR(42) NOT NULL,
                sorter VARCHAR(255) NOT NULL,
                trackers TEXT,
                date DATETIME NOT NULL,
                PRIMARY KEY (board_id, player_uuid)
                );
                            """)) {
            statement.execute();
        }
    }

    @Override
    public void unload() throws SQLException {
        this.connection.close();
    }

    @Override
    public void putPlayerDataForBoard(Board board, Map<Pair<String, UUID>, Pair<String, Map<String, String>>> sortedData) throws SQLException {
        try (PreparedStatement statement = this.connection.prepareStatement("""
                INSERT INTO `leaderboard` (board_id, player_uuid, player_name, sorter, trackers, date)
                VALUES (?, ?, ?, ?, ?, DATE('now')) ON CONFLICT(board_id, player_uuid) DO UPDATE SET player_name = ?, sorter = ?, trackers = ?, date = DATE('now');
                            """)) {
            for (Map.Entry<Pair<String, UUID>, Pair<String, Map<String, String>>> entry : sortedData.entrySet()) {
                statement.setString(1, board.getId());
                statement.setString(2, entry.getKey().getSecondValue().toString());
                statement.setString(3, entry.getKey().getFirstValue());
                String sorter = entry.getValue().getFirstValue();
                String trackers = GsonUtil.toBase64(GsonUtil.convertMapToJson(entry.getValue().getSecondValue()));
                statement.setString(4, sorter);
                statement.setString(5, trackers);
                statement.setString(6, entry.getKey().getFirstValue());
                statement.setString(7, sorter);
                statement.setString(8, trackers);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    @Override
    public Pair<Map<Pair<String, UUID>, Triplet<String, Map<String, String>, Integer>>,
            LinkedList<Quartet<Pair<String, UUID>, String, Map<String, String>, Integer>>> getDataForBoard
            (Set<Pair<String, UUID>> players, Board board, SQLDateType dateType) throws SQLException {
        Map<Pair<String, UUID>, Triplet<String, Map<String, String>, Integer>> onlinePlayersResult = new HashMap<>();
        LinkedList<Quartet<Pair<String, UUID>, String, Map<String, String>, Integer>> topPlayersResult = new LinkedList<>();
        String query = "SELECT player_name, player_uuid, sorter, trackers FROM `leaderboard` WHERE board_id = ?" +
                switch (dateType) {
                    case ALLTIME -> "";
                    case HOURLY -> " AND date BETWEEN DATE('now', '-1 hour') AND DATE('now')";
                    case DAILY -> " AND date BETWEEN DATE('now', '-1 day') AND DATE('now')";
                    case WEEKLY -> " AND date BETWEEN DATE('now', '-1 week') AND DATE('now')";
                    case MONTHLY -> " AND date BETWEEN DATE('now', '-1 month') AND DATE('now')";
                    case YEARLY -> " AND date BETWEEN DATE('now', '-1 year') AND DATE('now')";
                } + " ORDER BY sorter " + (board.getBoardSettings().isReversed() ? "DESC" : "ASC") + ";";
        try (PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setString(1, board.getId());
            int rank = 0;
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String name = resultSet.getString("player_name");
                    UUID uuid = UUID.fromString(resultSet.getString("player_uuid"));
                    Pair<String, UUID> player = Pair.of(name, uuid);
                    if (board.hasPlayerExempt(name))
                        continue;
                    if (this.plugin.getVaultPermissions().playerHas(Bukkit.getWorlds().get(0).getName(), Bukkit.getOfflinePlayer(uuid), "astralleaderboards.exempt." + board.getId()))
                        continue;
                    rank++; // increment the rank
                    String sorter = resultSet.getString("sorter");
                    Map<String, String> trackers = GsonUtil.convertJsonToMap(GsonUtil.fromBase64(resultSet.getString("trackers"))); // it converts the base64 string to a map using gson
                    if (rank <= board.getBoardSettings().getRowSize()) {
                        topPlayersResult.add(Quartet.of(player, sorter, trackers, rank));
                    }
                    if (players.contains(player)) {
                        onlinePlayersResult.put(player, Triplet.of(sorter, trackers, rank));
                    }
                }
            }
        }
        return Pair.of(onlinePlayersResult, topPlayersResult);
    }

    @Override
    public Triplet<String, Map<String, String>, Integer> getOnlinePlayerDataImmediately
            (Pair<String, UUID> player, Board board, SQLDateType dateType) throws SQLException {
        String query = board.getBoardSettings().isReversed() ?
                switch (dateType) {
                    case ALLTIME -> "SELECT sorter, trackers, RANK() OVER (ORDER BY sorter DESC) AS rank FROM `leaderboard` WHERE board_id = ? AND player_name = ? AND player_uuid = ?;";
                    case HOURLY -> "SELECT sorter, trackers, RANK() OVER (ORDER BY sorter DESC) AS rank FROM `leaderboard` WHERE board_id = ? AND player_name = ? AND player_uuid = ? AND `date` BETWEEN DATE('now','-1 hour') AND DATE('now');";
                    case DAILY -> "SELECT sorter, trackers, RANK() OVER (ORDER BY sorter DESC) AS rank FROM `leaderboard` WHERE board_id = ? AND player_name = ? AND player_uuid = ? AND `date` BETWEEN DATE('now','-1 day') AND DATE('now');";
                    case WEEKLY -> "SELECT sorter, trackers, RANK() OVER (ORDER BY sorter DESC) AS rank FROM `leaderboard` WHERE board_id = ? AND player_name = ? AND player_uuid = ? AND `date` BETWEEN DATE('now','-1 week') AND DATE('now');";
                    case MONTHLY -> "SELECT sorter, trackers, RANK() OVER (ORDER BY sorter DESC) AS rank FROM `leaderboard` WHERE board_id = ? AND player_name = ? AND player_uuid = ? AND `date` BETWEEN DATE('now','-1 month') AND DATE('now');";
                    case YEARLY -> "SELECT sorter, trackers, RANK() OVER (ORDER BY sorter DESC) AS rank FROM `leaderboard` WHERE board_id = ? AND player_name = ? AND player_uuid = ? AND `date` BETWEEN DATE('now','-1 year') AND DATE('now');";
                } :
                switch (dateType) {
                    case ALLTIME -> "SELECT sorter, trackers, RANK() OVER (ORDER BY sorter ASC) AS rank FROM `leaderboard` WHERE board_id = ? AND player_name = ? AND player_uuid = ?;";
                    case HOURLY -> "SELECT sorter, trackers, RANK() OVER (ORDER BY sorter ASC) AS rank FROM `leaderboard` WHERE board_id = ? AND player_name = ? AND player_uuid = ? AND `date` BETWEEN DATE('now','-1 hour') AND DATE('now');";
                    case DAILY -> "SELECT sorter, trackers, RANK() OVER (ORDER BY sorter ASC) AS rank FROM `leaderboard` WHERE board_id = ? AND player_name = ? AND player_uuid = ? AND `date` BETWEEN DATE('now','-1 day') AND DATE('now');";
                    case WEEKLY -> "SELECT sorter, trackers, RANK() OVER (ORDER BY sorter ASC) AS rank FROM `leaderboard` WHERE board_id = ? AND player_name = ? AND player_uuid = ? AND `date` BETWEEN DATE('now','-1 week') AND DATE('now');";
                    case MONTHLY -> "SELECT sorter, trackers, RANK() OVER (ORDER BY sorter ASC) AS rank FROM `leaderboard` WHERE board_id = ? AND player_name = ? AND player_uuid = ? AND `date` BETWEEN DATE('now','-1 month') AND DATE('now');";
                    case YEARLY -> "SELECT sorter, trackers, RANK() OVER (ORDER BY sorter ASC) AS rank FROM `leaderboard` WHERE board_id = ? AND player_name = ? AND player_uuid = ? AND `date` BETWEEN DATE('now','-1 year') AND DATE('now');";
                };
        try (PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setString(1, board.getId());
            statement.setString(2, player.getFirstValue());
            statement.setString(3, player.getSecondValue().toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String sorter = resultSet.getString("sorter");
                    Map<String, String> trackers = GsonUtil.convertJsonToMap(GsonUtil.fromBase64(resultSet.getString("trackers"))); // it converts the base64 string to a map using gson
                    int rank = resultSet.getInt("rank");
                    return Triplet.of(sorter, trackers, rank);
                }
            }
        }
        return null;
    }
}

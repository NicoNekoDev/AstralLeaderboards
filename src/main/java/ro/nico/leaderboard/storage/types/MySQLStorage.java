package ro.nico.leaderboard.storage.types;

import io.github.NicoNekoDev.SimpleTuples.Pair;
import io.github.NicoNekoDev.SimpleTuples.Quartet;
import io.github.NicoNekoDev.SimpleTuples.Triplet;
import ro.nico.leaderboard.AstralLeaderboardsPlugin;
import ro.nico.leaderboard.api.Board;
import ro.nico.leaderboard.storage.SQLDateType;
import ro.nico.leaderboard.storage.settings.StorageMySQLSettings;
import ro.nico.leaderboard.storage.settings.StorageSettings;
import ro.nico.leaderboard.util.GsonUtil;

import java.sql.*;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MySQLStorage extends Storage {
    private Connection connection;
    private String table_prefix = "astrallb_";

    public MySQLStorage(AstralLeaderboardsPlugin plugin) {
        super(plugin);
    }

    public void load(StorageSettings settings) throws SQLException {
        StorageMySQLSettings mySQLSettings = settings.getMySQLSettings();
        String user = mySQLSettings.getUsername();
        String pass = mySQLSettings.getPassword();
        String host = mySQLSettings.getHost();
        int port = mySQLSettings.getPort();
        String database = mySQLSettings.getDatabase();
        boolean sslEnabled = mySQLSettings.isSSLEnabled();
        this.table_prefix = mySQLSettings.getTablePrefix();
        this.connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?user=" + user + "&password=" + pass + "&useSSL=" + sslEnabled + "&autoReconnect=true");

        try (PreparedStatement statement = this.connection.prepareStatement("""
                CREATE TABLE IF NOT EXISTS ? (
                board_id VARCHAR(255) NOT NULL,
                player_name VARCHAR(255) NOT NULL,
                player_uuid VARCHAR(42) NOT NULL,
                sorter VARCHAR(255) NOT NULL,
                trackers TEXT,
                date DATETIME NOT NULL,
                CONSTRAINT unique_id PRIMARY KEY (board_id, player_name, player_uuid)
                );
                            """)) {
            statement.setString(1, this.table_prefix + "leaderboard");
            statement.execute();
        }
    }

    @Override
    public void unload() throws SQLException {
        this.connection.close();
    }

    @Override
    public void putPlayerDataForBoard(Board board, Map<Pair<String, UUID>, Pair<String, Map<String, String>>> sortedData) {
        try (PreparedStatement statement = this.connection.prepareStatement("""
                INSERT INTO ? (board_id, player_name, player_uuid, sorter, trackers, date)
                VALUES (?, ?, ?, ?, ?, DATE('now')) ON DUPLICATE KEY UPDATE sorter = ?, trackers = ?, date = NOW();
                            """)) {
            for (Map.Entry<Pair<String, UUID>, Pair<String, Map<String, String>>> entry : sortedData.entrySet()) {
                statement.setString(1, this.table_prefix + "leaderboard");
                statement.setString(2, board.getId());
                statement.setString(3, entry.getKey().getFirstValue());
                statement.setString(4, entry.getKey().getSecondValue().toString());
                String sorter = entry.getValue().getFirstValue();
                String trackers = GsonUtil.toBase64(GsonUtil.convertMapToJson(entry.getValue().getSecondValue()));
                statement.setString(5, sorter);
                statement.setString(6, trackers);
                statement.setString(7, sorter);
                statement.setString(8, trackers);
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public LinkedList<Quartet<Pair<String, UUID>, String, Map<String, String>, Integer>> getPlayersDataForBoard(Board board, SQLDateType dateType) throws SQLException {
        LinkedList<Quartet<Pair<String, UUID>, String, Map<String, String>, Integer>> result = new LinkedList<>();
        String query = board.getBoardSettings().isReversed() ?
                switch (dateType) {
                    case ALLTIME -> "SELECT player_name, player_uuid, sorter, trackers FROM ? WHERE `board_id` = ? ORDER BY `sorter` DESC LIMIT ?;";
                    case HOURLY -> "SELECT player_name, player_uuid, sorter, trackers FROM ? WHERE `board_id` = ? AND `date` BETWEEN NOW() AND DATE_SUB(NOW(), INTERVAL 1 HOUR) ORDER BY sorter DESC LIMIT ?;";
                    case DAILY -> "SELECT player_name, player_uuid, sorter, trackers FROM ? WHERE `board_id` = ? AND `date` BETWEEN NOW() AND DATE_SUB(NOW(), INTERVAL 1 DAY) ORDER BY sorter DESC LIMIT ?;";
                    case WEEKLY -> "SELECT player_name, player_uuid, sorter, trackers FROM ? WHERE `board_id` = ? AND `date` BETWEEN NOW() AND DATE_SUB(NOW(), INTERVAL 1 WEEK) ORDER BY sorter DESC LIMIT ?;";
                    case MONTHLY -> "SELECT player_name, player_uuid, sorter, trackers FROM ? WHERE `board_id` = ? AND `date` BETWEEN NOW() AND DATE_SUB(NOW(), INTERVAL 1 MONTH) ORDER BY sorter DESC LIMIT ?;";
                    case YEARLY -> "SELECT player_name, player_uuid, sorter, trackers FROM ? WHERE `board_id` = ? AND `date` BETWEEN NOW() AND DATE_SUB(NOW(), INTERVAL 1 YEAR) ORDER BY sorter DESC LIMIT ?;";
                } :
                switch (dateType) {
                    case ALLTIME -> "SELECT player_name, player_uuid, sorter, trackers FROM ? WHERE `board_id` = ? ORDER BY `sorter` DESC LIMIT ?;";
                    case HOURLY -> "SELECT player_name, player_uuid, sorter, trackers FROM ? WHERE `board_id` = ? AND `date` BETWEEN NOW() AND DATE_SUB(NOW(), INTERVAL 1 HOUR) ORDER BY sorter ASC LIMIT ?;";
                    case DAILY -> "SELECT player_name, player_uuid, sorter, trackers FROM ? WHERE `board_id` = ? AND `date` BETWEEN NOW() AND DATE_SUB(NOW(), INTERVAL 1 DAY) ORDER BY sorter ASC LIMIT ?;";
                    case WEEKLY -> "SELECT player_name, player_uuid, sorter, trackers FROM ? WHERE `board_id` = ? AND `date` BETWEEN NOW() AND DATE_SUB(NOW(), INTERVAL 1 WEEK) ORDER BY sorter ASC LIMIT ?;";
                    case MONTHLY -> "SELECT player_name, player_uuid, sorter, trackers FROM ? WHERE `board_id` = ? AND `date` BETWEEN NOW() AND DATE_SUB(NOW(), INTERVAL 1 MONTH) ORDER BY sorter ASC LIMIT ?;";
                    case YEARLY -> "SELECT player_name, player_uuid, sorter, trackers FROM ? WHERE `board_id` = ? AND `date` BETWEEN NOW() AND DATE_SUB(NOW(), INTERVAL 1 YEAR) ORDER BY sorter ASC LIMIT ?;";
                };
        try (PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setString(1, this.table_prefix + "leaderboard");
            statement.setString(2, board.getId());
            statement.setInt(3, board.getBoardSettings().getRowSize());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Pair<String, UUID> player = new Pair<>(resultSet.getString("player_name"), UUID.fromString(resultSet.getString("player_uuid")));
                    String sorter = resultSet.getString("sorter");
                    Map<String, String> trackers = GsonUtil.convertJsonToMap(GsonUtil.fromBase64(resultSet.getString("trackers"))); // it converts the base64 string to a map using gson
                    result.add(Quartet.of(player, sorter, trackers, -1)); // todo: rank
                }
            }
        }
        return result;
    }

    @Override
    public Map<Pair<String, UUID>, Triplet<String, Map<String, String>, Integer>> getOnlinePlayersDataForBoard(Set<Pair<String, UUID>> players, Board board, SQLDateType dateType) throws SQLException {
        return null;
    }

    @Override
    public Triplet<String, Map<String, String>, Integer> getOnlinePlayerDataImmediately(Pair<String, UUID> player, Board board, SQLDateType dateType) throws SQLException {
        return null;
    }
}

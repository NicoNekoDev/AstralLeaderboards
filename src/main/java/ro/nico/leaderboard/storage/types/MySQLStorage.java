package ro.nico.leaderboard.storage.types;

import io.github.NicoNekoDev.SimpleTuples.Pair;
import io.github.NicoNekoDev.SimpleTuples.Triplet;
import org.bukkit.configuration.file.YamlConfiguration;
import ro.nico.leaderboard.AstralLeaderboardsPlugin;
import ro.nico.leaderboard.api.Board;
import ro.nico.leaderboard.storage.SQLDateType;
import ro.nico.leaderboard.util.GsonUtil;

import java.sql.*;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

public class MySQLStorage extends Storage {
    private Connection connection;
    private String table_prefix = "astrallb_";

    public MySQLStorage(AstralLeaderboardsPlugin plugin) {
        super(plugin);
    }

    public void load(YamlConfiguration settings) throws SQLException {
        String user = settings.getString("storage.mysql.user", "user");
        String pass = settings.getString("storage.mysql.password", "password");
        String ip = settings.getString("storage.mysql.ip", "localhost");
        String port = "3306";
        if (settings.isString("storage.mysql.port")) {
            port = settings.getString("storage.mysql.port", "3306");
        } else if (settings.isInt("storage.mysql.port")) {
            port = String.valueOf(settings.getInt("storage.mysql.port", 3306));
        }
        String database = settings.getString("storage.mysql.name", "database");
        boolean sslEnabled = settings.getBoolean("database.mysql.enable_ssl", false);
        this.table_prefix = settings.getString("storage.mysql.table_prefix", "astrallb_");
        this.connection = DriverManager.getConnection("jdbc:mysql://" + ip + ":" + port + "/" + database + "?user=" + user + "&password=" + pass + "&useSSL=" + sslEnabled + "&autoReconnect=true");

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
    public LinkedList<Triplet<Pair<String, UUID>, String, Map<String, String>>> getPlayerDataForBoard(Board board, SQLDateType dateType) throws SQLException {
        LinkedList<Triplet<Pair<String, UUID>, String, Map<String, String>>> result = new LinkedList<>();
        String query = board.isReversed() ?
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
            statement.setInt(3, board.getRowSize());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Pair<String, UUID> player = new Pair<>(resultSet.getString("player_name"), UUID.fromString(resultSet.getString("player_uuid")));
                    String sorter = resultSet.getString("sorter");
                    Map<String, String> trackers = GsonUtil.convertJsonToMap(GsonUtil.fromBase64(resultSet.getString("trackers"))); // it converts the base64 string to a map using gson
                    result.add(new Triplet<>(player, sorter, trackers));
                }
            }
        }
        return result;
    }
}

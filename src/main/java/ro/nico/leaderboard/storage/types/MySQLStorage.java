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
import java.util.*;

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
    public void putPlayerDataForBoard(Board board, Map<Pair<String, UUID>, Pair<String, Map<String, String>>> sortedData) throws SQLException {
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
        }
    }

    @Override
    public Pair<Map<Pair<String, UUID>, Triplet<String, Map<String, String>, Integer>>, LinkedList<Quartet<Pair<String, UUID>, String, Map<String, String>, Integer>>> getDataForBoard(Set<Pair<String, UUID>> players, Board board, SQLDateType dateType) throws SQLException {
        return null;
    }

    @Override
    public Triplet<String, Map<String, String>, Integer> getOnlinePlayerDataImmediately(Pair<String, UUID> player, Board board, SQLDateType dateType) throws SQLException {
        return null;
    }
}

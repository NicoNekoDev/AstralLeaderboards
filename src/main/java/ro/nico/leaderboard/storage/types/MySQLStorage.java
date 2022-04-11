package ro.nico.leaderboard.storage.types;

import org.bukkit.Bukkit;
import ro.nico.leaderboard.AstralLeaderboardsPlugin;
import ro.nico.leaderboard.api.Board;
import ro.nico.leaderboard.data.PlayerData;
import ro.nico.leaderboard.data.PlayerId;
import ro.nico.leaderboard.storage.SQLDateType;
import ro.nico.leaderboard.storage.settings.StorageMySQLSettings;
import ro.nico.leaderboard.storage.settings.StorageSettings;
import ro.nico.leaderboard.util.GsonUtil;

import java.sql.*;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

public class MySQLStorage extends Storage {
    private Connection connection;
    private String tablePrefix = "";

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
        this.tablePrefix = mySQLSettings.getTablePrefix();
        this.connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?user=" + user + "&password=" + pass + "&useSSL=" + sslEnabled + "&autoReconnect=true");
        try (PreparedStatement statement = this.connection.prepareStatement("""
                CREATE TABLE IF NOT EXISTS ? (
                board_id VARCHAR(255) NOT NULL,
                player_name VARCHAR(255) NOT NULL,
                player_uuid VARCHAR(42) NOT NULL,
                sorter VARCHAR(255) NOT NULL,
                trackers TEXT,
                date DATETIME NOT NULL,
                CONSTRAINT unique_id PRIMARY KEY (board_id, player_uuid)
                );
                            """)) {
            statement.setString(1, this.tablePrefix + "leaderboard");
            statement.execute();
        }
    }

    @Override
    public void unload() throws SQLException {
        this.connection.close();
    }

    @Override
    public void putDataForBoard(Board board, Map<PlayerId, PlayerData> data) throws SQLException {
        try (PreparedStatement statement = this.connection.prepareStatement("""
                INSERT INTO ? (board_id, player_uuid, player_name, sorter, trackers, date)
                VALUES (?, ?, ?, ?, ?, NOW()) ON DUPLICATE KEY UPDATE player_name = ?, sorter = ?, trackers = ?, date = NOW();
                            """)) {
            for (Map.Entry<PlayerId, PlayerData> entry : data.entrySet()) {
                statement.setString(1, this.tablePrefix + "leaderboard");
                statement.setString(2, board.getId());
                statement.setString(3, entry.getKey().getUuid().toString());
                statement.setString(4, entry.getKey().getName());
                String sorter = entry.getValue().getSorter();
                String trackers = GsonUtil.toBase64(GsonUtil.convertMapToJson(entry.getValue().getTrackers()));
                statement.setString(5, sorter);
                statement.setString(6, trackers);
                statement.setString(7, entry.getKey().getName());
                statement.setString(8, sorter);
                statement.setString(9, trackers);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    @Override
    public void getDataForBoard(ConcurrentMap<Integer, PlayerId> rankMap, ConcurrentMap<PlayerId, PlayerData> dataMap, Board board, SQLDateType dateType) throws SQLException {
        String query = "SELECT player_name, player_uuid, sorter, trackers FROM ? WHERE board_id = ?" + switch (dateType) {
            case ALLTIME -> "";
            case HOURLY -> " AND date BETWEEN DATE_SUB(NOW(), INTERVAL 1 HOUR) AND NOW()";
            case DAILY -> " AND date BETWEEN DATE_ADD(NOW(),INTERVAL -1 DAY) AND NOW()";
            case WEEKLY -> " AND date BETWEEN DATE_ADD(NOW(),INTERVAL -1 WEEK) AND NOW()";
            case MONTHLY -> " AND date BETWEEN DATE_ADD(NOW(),INTERVAL -1 MONTH) AND NOW()";
            case YEARLY -> " AND date BETWEEN DATE_ADD(NOW(),INTERVAL -1 YEAR) AND NOW()";
        } + " ORDER BY sorter " + (board.getBoardSettings().isReversed() ? "DESC" : "ASC") + ";";
        try (PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setString(1, this.tablePrefix + "leaderboard");
            statement.setString(2, board.getId());
            int rank = 0;
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String name = resultSet.getString("player_name");
                    UUID uuid = UUID.fromString(resultSet.getString("player_uuid"));
                    if (board.hasPlayerExempt(name)) continue;
                    if (this.plugin.getVaultPerms()
                            .playerHas(Bukkit.getWorlds().get(0).getName(), Bukkit.getOfflinePlayer(uuid), "astralleaderboards.exempt." + board.getId()))
                        continue;
                    rank++; // increment the rank
                    String sorter = resultSet.getString("sorter");
                    Map<String, String> trackers = GsonUtil.convertJsonToMap(GsonUtil.fromBase64(resultSet.getString("trackers"))); // it converts the base64 string to a map using gson
                    PlayerId playerId = new PlayerId(name, uuid);
                    PlayerData playerData = new PlayerData(sorter, trackers, rank);
                    rankMap.put(rank, playerId);
                    dataMap.put(playerId, playerData);
                }
            }
        }
    }

}

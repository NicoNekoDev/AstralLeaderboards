package ro.nico.leaderboard.storage.types;

import io.github.NicoNekoDev.SimpleTuples.Pair;
import org.bukkit.Bukkit;
import ro.nico.leaderboard.AstralLeaderboardsPlugin;
import ro.nico.leaderboard.api.Board;
import ro.nico.leaderboard.data.PlayerData;
import ro.nico.leaderboard.data.PlayerId;
import ro.nico.leaderboard.storage.SQLDateType;
import ro.nico.leaderboard.storage.settings.StorageSettings;
import ro.nico.leaderboard.util.GsonUtil;

import java.io.File;
import java.sql.*;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

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
    public void putDataForBoard(Board board, Map<PlayerId, PlayerData> data) throws SQLException {
        try (PreparedStatement statement = this.connection.prepareStatement("""
                INSERT INTO `leaderboard` (board_id, player_uuid, player_name, sorter, trackers, date)
                VALUES (?, ?, ?, ?, ?, DATE('now')) ON CONFLICT(board_id, player_uuid) DO UPDATE SET player_name = ?, sorter = ?, trackers = ?, date = DATE('now');
                            """)) {
            for (Map.Entry<PlayerId, PlayerData> entry : data.entrySet()) {
                statement.setString(1, board.getId());
                statement.setString(2, entry.getKey().getUuid().toString());
                statement.setString(3, entry.getKey().getName());
                String sorter = entry.getValue().getSorter();
                String trackers = GsonUtil.toBase64(GsonUtil.convertMapToJson(entry.getValue().getTrackers()));
                statement.setString(4, sorter);
                statement.setString(5, trackers);
                statement.setString(6, entry.getKey().getName());
                statement.setString(7, sorter);
                statement.setString(8, trackers);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    @Override
    public void getDataForBoard(Pair<ConcurrentMap<Integer, PlayerId>, ConcurrentMap<PlayerId, PlayerData>> data, Board board, SQLDateType dateType) throws SQLException {
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
                    if (board.hasPlayerExempt(name))
                        continue;
                    if (this.plugin.getVaultPermissions().playerHas(Bukkit.getWorlds().get(0).getName(), Bukkit.getOfflinePlayer(uuid), "astralleaderboards.exempt." + board.getId()))
                        continue;
                    rank++; // increment the rank
                    String sorter = resultSet.getString("sorter");
                    Map<String, String> trackers = GsonUtil.convertJsonToMap(GsonUtil.fromBase64(resultSet.getString("trackers"))); // it converts the base64 string to a map using gson
                    PlayerId playerId = new PlayerId(name, uuid);
                    PlayerData playerData = new PlayerData(sorter, trackers, rank);
                    data.getFirstValue().put(rank, playerId);
                    data.getSecondValue().put(playerId, playerData);
                }
            }
        }
    }
}

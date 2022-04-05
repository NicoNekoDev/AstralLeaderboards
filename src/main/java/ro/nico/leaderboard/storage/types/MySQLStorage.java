package ro.nico.leaderboard.storage.types;

import io.github.NicoNekoDev.SimpleTuples.Pair;
import ro.nico.leaderboard.AstralLeaderboardsPlugin;
import ro.nico.leaderboard.api.Board;
import ro.nico.leaderboard.data.PlayerData;
import ro.nico.leaderboard.data.PlayerId;
import ro.nico.leaderboard.storage.SQLDateType;
import ro.nico.leaderboard.storage.settings.StorageMySQLSettings;
import ro.nico.leaderboard.storage.settings.StorageSettings;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

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
    public void putDataForBoard(Board board, Map<PlayerId, PlayerData> data) throws SQLException {

    }

    @Override
    public void getDataForBoard(Pair<ConcurrentMap<Integer, PlayerId>, ConcurrentMap<PlayerId, PlayerData>> pair, Board board, SQLDateType dateType) throws SQLException {

    }

}

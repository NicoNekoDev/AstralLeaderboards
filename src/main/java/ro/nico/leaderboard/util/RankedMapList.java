package ro.nico.leaderboard.util;

import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import ro.nico.leaderboard.api.Board;
import ro.nico.leaderboard.data.PlayerData;
import ro.nico.leaderboard.data.PlayerId;
import ro.nico.leaderboard.storage.SQLDateType;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

public class RankedMapList {
    private final HTreeMap<Integer, PlayerId> rankList;
    private final HTreeMap<PlayerId, PlayerData> map;

    public RankedMapList(Board board, SQLDateType dateType) throws IOException {
        this.rankList = board.getPlugin().getMapDB().hashMap("ranks_" + board.getId() + "_" + dateType.name().toLowerCase(), Serializer.INTEGER, PlayerId.SERIALIZER).createOrOpen();
        this.map = board.getPlugin().getMapDB().hashMap("map_" + board.getId() + "_" + dateType.name().toLowerCase(), PlayerId.SERIALIZER, PlayerData.SERIALIZER).createOrOpen();
    }

    public void unload() {
        this.rankList.close();
        this.map.close();
    }

    public void update(Board board, SQLDateType dateType) throws SQLException {
        // this.clear(); < do we need clear?
        board.getPlugin().getStorage().getDataForBoard(this.rankList, this.map, board, dateType);
    }

    public PlayerData getValueByKey(PlayerId key) {
        return map.get(key);
    }

    public PlayerData getValueByRank(int rank) {
        return getValueByKey(getKeyByRank(rank));
    }

    public PlayerId getKeyByRank(int rank) {
        return rankList.get(rank);
    }

    public Map<PlayerId, PlayerData> asMap() {
        return map;
    }

    public boolean containsKey(PlayerId key) {
        return map.containsKey(key);
    }

    public boolean containsValue(PlayerData value) {
        return map.containsValue(value);
    }

    public boolean containsRank(int rank) {
        return rankList.containsKey(rank);
    }

}

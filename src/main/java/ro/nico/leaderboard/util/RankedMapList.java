package ro.nico.leaderboard.util;

import io.github.NicoNekoDev.SimpleTuples.Pair;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import ro.nico.leaderboard.api.Board;
import ro.nico.leaderboard.data.PlayerData;
import ro.nico.leaderboard.data.PlayerId;
import ro.nico.leaderboard.storage.SQLDateType;

import java.io.File;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

public class RankedMapList {
    private final ConcurrentMap<Integer, PlayerId> rankList;
    private final ConcurrentMap<PlayerId, PlayerData> map;
    private final File cacheFile;
    private final DB mapdb;

    public RankedMapList(Board board) {
        this.cacheFile = new File(board.getPlugin().getCacheDir(), UUID.randomUUID() + ".cache");
        this.mapdb = DBMaker.fileDB(cacheFile).fileMmapEnableIfSupported().make();
        this.rankList = this.mapdb.hashMap("ranks").keySerializer(Serializer.INTEGER).valueSerializer(PlayerId.SERIALIZER).create();
        this.map = this.mapdb.hashMap("data").keySerializer(PlayerId.SERIALIZER).valueSerializer(PlayerData.SERIALIZER).create();
    }

    public void update(Board board, SQLDateType dateType) throws SQLException {
        // this.clear(); < do we need clear?
        board.getPlugin().getStorage().getDataForBoard(Pair.of(this.rankList, this.map), board, dateType);
    }

    public void unload() {
        this.mapdb.close();
        this.cacheFile.delete();
    }

    protected void put(int rank, PlayerId key, PlayerData value) {
        this.map.put(key, value);
        this.rankList.put(rank, key);
    }

    public PlayerData getByKey(PlayerId key) {
        return map.get(key);
    }

    public PlayerData getByRank(int rank) {
        return getByKey(rankList.get(rank));
    }

    public Map<PlayerId, PlayerData> asMap() {
        return map;
    }

    public void remove(int rank) {
        map.remove(rankList.remove(rank));
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

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public void clear() {
        rankList.clear();
        map.clear();
    }
}

package ro.nico.leaderboard.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.Serializer;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

// thank you java, I hate it.
@AllArgsConstructor
public class PlayerData {
    @Getter @NonNull private final String sorter;
    @Getter @NonNull private final Map<String, String> trackers;
    @Getter private final int rank;

    protected int getTrackersSize() {
        return this.trackers.size();
    }


    public static final Serializer<PlayerData> SERIALIZER = new Serializer<>() {
        @Override
        public void serialize(@NotNull DataOutput2 dataOutput2, @NotNull PlayerData playerData) throws IOException {
            dataOutput2.writeUTF(playerData.getSorter());
            dataOutput2.writeInt(playerData.getTrackersSize());
            for (Map.Entry<String, String> entry : playerData.getTrackers().entrySet()) {
                dataOutput2.writeUTF(entry.getKey());
                dataOutput2.writeUTF(entry.getValue());
            }
            dataOutput2.writeInt(playerData.getRank());
        }

        @Override
        public PlayerData deserialize(@NotNull DataInput2 dataInput2, int i) throws IOException {
            String sorter = dataInput2.readUTF();
            int trackersSize = dataInput2.readInt();
            Map<String, String> trackers = new LinkedHashMap<>(trackersSize);
            for (int j = 0; j < trackersSize; j++) {
                String key = dataInput2.readUTF();
                String value = dataInput2.readUTF();
                trackers.put(key, value);
            }
            int rank = dataInput2.readInt();
            return new PlayerData(sorter, trackers, rank);
        }
    };
}

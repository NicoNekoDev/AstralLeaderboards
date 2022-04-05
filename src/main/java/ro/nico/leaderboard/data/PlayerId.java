package ro.nico.leaderboard.data;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.Serializer;

import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;

@AllArgsConstructor
@EqualsAndHashCode
public class PlayerId implements Serializable {
    @Getter @NonNull private final String name;
    @Getter @NonNull private final UUID uuid;

    public static final Serializer<PlayerId> SERIALIZER = new Serializer<>() {
        @Override
        public void serialize(@NotNull DataOutput2 dataOutput2, @NotNull PlayerId playerId) throws IOException {
            dataOutput2.writeUTF(playerId.getName());
            dataOutput2.writeUTF(playerId.getUuid().toString());
        }

        @Override
        public PlayerId deserialize(@NotNull DataInput2 dataInput2, int i) throws IOException {
            return new PlayerId(dataInput2.readUTF(), java.util.UUID.fromString(dataInput2.readUTF()));
        }
    };
}

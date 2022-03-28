package ro.nico.leaderboard.storage.cache;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class PlayerData {
    private final String sorter;
    private final Map<String, String> trackers;
    private final int rank;

    public PlayerData(final String sorter, final Map<String, String> trackers, int rank) {
        this.sorter = sorter;
        this.trackers = trackers;
        this.rank = rank;
    }

    public int getRank() {
        return this.rank;
    }

    @NotNull
    public String getSorter() {
        return this.sorter;
    }

    @Nullable
    public String getTracker(final String key) {
        return this.trackers.get(key);
    }
}

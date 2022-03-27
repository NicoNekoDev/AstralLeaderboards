package ro.nico.leaderboard.storage.cache;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class PlayerData {
    private final String sorter;
    private final Map<String, String> trackers;

    public PlayerData(final String sorter, final Map<String, String> trackers) {
        this.sorter = sorter;
        this.trackers = trackers;
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

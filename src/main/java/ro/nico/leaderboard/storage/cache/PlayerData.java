package ro.nico.leaderboard.storage.cache;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class PlayerData {
    @Getter @NonNull private final String sorter;
    @Getter @NonNull private final Map<String, String> trackers;
    @Getter private final int rank;
}

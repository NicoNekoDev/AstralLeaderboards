package ro.nico.leaderboard.util;

import com.mrivanplays.annotationconfig.core.annotations.Key;
import com.mrivanplays.annotationconfig.core.annotations.comment.Comment;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@NoArgsConstructor
public class BoardSettings {

    public BoardSettings(@NotNull String id) {
        this.id = id;
    }

    @NonNull
    @Getter
    @Comment("The id of the board used")
    @Comment("Must contain only letters, numbers and dashes")
    @Key("board-id")
    private String id = "default";

    @NonNull
    @Getter
    @Setter
    @Comment("The value used to sort the leaderboard")
    @Key("sorter")
    private String sorter = "%player_name%";

    @Getter
    @Setter
    @Comment("Interval in seconds between leaderboard updates")
    @Key("update-interval")
    private int updateInterval = 30;

    @Getter
    @Setter
    @Comment("Interval in ticks used for async tasks checks")
    @Key("heartbeat-interval")
    private int heartbeatInterval = 20;

    @Getter
    @Setter
    @Comment("If the leaderboard is reversed")
    @Key("reversed")
    private boolean reversed = false;

    @Getter
    @Setter
    @Comment("The amount of rows to load from the database for the top players")
    @Comment("This setting would affect the performance of the leaderboard update")
    @Key("row-size")
    private int rowSize = 15;

    @NonNull
    @Getter
    @Setter
    @Comment("The default tracker placeholder if no data was found")
    @Key("default-tracker")
    private String defaultTrackerPlaceholder = "---";

    @Getter
    @Setter
    @Comment("If the leaderboard should use the 'hourly' date type")
    @Key("update.hourly")
    private boolean hourlyUpdated = false;

    @Getter
    @Setter
    @Comment("If the leaderboard should use the 'daily' date type")
    @Key("update.daily")
    private boolean dailyUpdated = false;

    @Getter
    @Setter
    @Comment("If the leaderboard should use the 'weekly' date type")
    @Key("update.weekly")
    private boolean weeklyUpdated = false;

    @Getter
    @Setter
    @Comment("If the leaderboard should use the 'monthly' date type")
    @Key("update.monthly")
    private boolean monthlyUpdated = false;

    @Getter
    @Setter
    @Comment("If the leaderboard should use the 'yearly' date type")
    @Key("update.yearly")
    private boolean yearlyUpdated = false;

    @NonNull
    @Getter
    @Comment("Trackers used by the leaderboard display.")
    @Comment("For example in the placeholder %astrallb_<board>_<position>_<tracker>_<date>%")
    @Comment("the '<tracker>' is used to get the data from here.")
    @Comment("")
    @Comment("trackers:")
    @Comment("  display: \"%player_displayname%\"")
    @Comment("  kills: \"%player_kills%\"")
    @Comment("")
    @Comment("Here the trackers will be 'display' and 'kills'")
    @Key("trackers")
    private final Map<String, Object> trackers = new LinkedHashMap<>() {
        {
            put("display", "%player_displayname%");
            put("kills", "%player_kills%");
        }
    };

    @Getter
    @Comment("A list of exempted players names from the leaderboard who's values will not be displayed")
    @Key("exempted-players.names")
    private final Set<String> exemptPlayersNames = new LinkedHashSet<>() {
        {
            add("ExamplePlayerName");
        }
    };

    @Getter
    @Comment("A list of exempted players uuids from the leaderboard who's values will not be displayed")
    @Key("exempted-players.uuids")
    private final Set<String> exemptPlayersUUIDs = new HashSet<>();
}

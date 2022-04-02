package ro.nico.leaderboard.settings;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SuppressWarnings("FieldMayBeFinal")
public class BoardSettings {

    public BoardSettings() {
        this.id = "default";
        this.sorter = "%player_name%";
    }

    public BoardSettings(@NotNull String id, @NotNull String sorter) {
        this.id = id;
        this.sorter = sorter;
    }


    @Getter
    @SerializedName("board-id")
    private String id;

    @Getter
    @Setter
    private String sorter;

    @Getter
    private Map<String, String> trackers = new LinkedHashMap<>() {
        {
            put("name", "%player_name%");
            put("sorter", sorter);
        }
    };

    @SerializedName("_comment-update-interval") private String updateIntervalComment = "Interval in seconds between placeholders updates";

    @Getter
    @Setter
    @SerializedName("update-interval")
    private int updateInterval = 30;

    @SerializedName("_comment-heartbeat-interval") private String heartbeatIntervalComment = "Interval in ticks used for async tasks checks";

    @Getter
    @Setter
    @SerializedName("heartbeat-interval")
    private int heartbeatInterval = 20;

    @SerializedName("_comment-row-size-1") private String rowSizeComment1 = "The amount of rows to load from the database for the top players.";
    @SerializedName("_comment-row-size-2") private String rowSizeComment2 = "This setting would affect the performance of the leaderboard update.";

    @Getter
    @Setter
    @SerializedName("row-size")
    private int rowSize = 15;

    @SerializedName("_comment-reversed") private String reversedComment = "If true, the leaderboard will be sorted in descending order.";

    @Getter
    @Setter
    private boolean reversed = false;

    @SerializedName("_comment-default-tracker") private String defaultTrackerComment = "The default tracker placeholder if no data was found";

    @Getter
    @Setter
    @SerializedName("default-tracker")
    private String defaultTrackerPlaceholder = "---";

    @SerializedName("_comment-update-1") private String updateComment1 = "If the leaderboard should use any update interval.";
    @SerializedName("_comment-update-2") private String updateComment2 = "This would affect the performance of the leaderboard update.";

    @Getter
    @SerializedName("update")
    private UpdateSettings updateSettings = new UpdateSettings();

    @Getter
    @SerializedName("exempt-players")
    private ExemptPlayersSettings exemptPlayersSettings = new ExemptPlayersSettings();

    public static class UpdateSettings {
        @Getter
        @Setter
        @SerializedName("hourly")
        private boolean hourlyUpdated = false;

        @Getter
        @Setter
        @SerializedName("daily")
        private boolean dailyUpdated = false;

        @Getter
        @Setter
        @SerializedName("weekly")
        private boolean weeklyUpdated = false;

        @Getter
        @Setter
        @SerializedName("monthly")
        private boolean monthlyUpdated = false;

        @Getter
        @Setter
        @SerializedName("yearly")
        private boolean yearlyUpdated = false;
    }

    public static class ExemptPlayersSettings {
        @Getter
        @SerializedName("names")
        private Set<String> exemptPlayersNames = new LinkedHashSet<>() {
            {
                add("ExamplePlayerName");
            }
        };

        @Getter
        @SerializedName("uuids")
        private Set<String> exemptPlayersUUIDs = new LinkedHashSet<>();
    }
}

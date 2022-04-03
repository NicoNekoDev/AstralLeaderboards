package ro.nico.leaderboard.settings;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import ro.nico.leaderboard.util.SettingsUtil;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class BoardSettings implements SettingsSerializer {

    public BoardSettings() {
        this("default", "%player_name%");
    }

    public BoardSettings(@NotNull String id, @NotNull String sorter) {
        this.id = id;
        this.sorter = sorter;
        YamlConfiguration defaultTrackers = new YamlConfiguration();
        defaultTrackers.set("display", "%player_name%");
        defaultTrackers.set("sorter", "%sorter%");
        this.trackers = defaultTrackers;
    }

    @Override
    public void load(ConfigurationSection section) {
        this.id = SettingsUtil.getOrSetStringFunction().apply(section, "board-id", this.id,
                Optional.of(List.of("Board id, must be unique")));
        this.sorter = SettingsUtil.getOrSetStringFunction().apply(section, "sorter", this.sorter,
                Optional.of(List.of("Sorter of the board, used for identification.")));
        this.trackers = SettingsUtil.getOrSetSectionFunction().apply(section, "trackers", this.trackers,
                Optional.of(List.of(
                        "A list of trackers to be displayed on placeholders.",
                        "An example tracker is:",
                        "  display: \"%player_name%\"",
                        "Which can be used in the placeholder %astrallb_<board>_<position>_display_<date>%",
                        "where 'display' is the tracker id."
                )));
        this.updateInterval = SettingsUtil.getOrSetIntFunction().apply(section, "update-interval", this.updateInterval,
                Optional.of(List.of("Update interval in seconds.", "Default is 30 seconds.")));
        this.heartbeatInterval = SettingsUtil.getOrSetIntFunction().apply(section, "heartbeat-interval", this.heartbeatInterval,
                Optional.of(List.of("Heartbeat interval in ticks.", "It's being used for multi-threading checks.", "Default is 20 ticks.")));
        this.rowSize = SettingsUtil.getOrSetIntFunction().apply(section, "row-size", this.rowSize,
                Optional.of(List.of("The 'top' amount of cached values from the database.", "Default is 15.")));
        this.reversed = SettingsUtil.getOrSetBooleanFunction().apply(section, "reversed", this.reversed,
                Optional.of(List.of("If the board sorting order should be reversed.", "Default is false.")));
        this.defaultTrackerPlaceholder = SettingsUtil.getOrSetStringFunction().apply(section, "default-tracker", this.defaultTrackerPlaceholder,
                Optional.of(List.of("The default tracker placeholder to be used if no tracker is found in the database.", "Default is \"---\".")));
        this.updateSettings.load(SettingsUtil.getOrCreateSection().apply(section, "updates", Optional.of(List.of(""))));
        this.exemptPlayersNames.addAll(SettingsUtil.getOrSetStringCollectionFunction().apply(section, "exempt-players.names", this.exemptPlayersNames, Optional.empty()));
        this.exemptPlayersUUIDs.addAll(SettingsUtil.getOrSetStringCollectionFunction().apply(section, "exempt-players.uuids", this.exemptPlayersUUIDs, Optional.empty()));
    }

    @Getter
    private String id;

    @Getter
    @Setter
    private String sorter;

    @Getter
    private ConfigurationSection trackers;

    @Getter
    @Setter
    private int updateInterval = 30;


    @Getter
    @Setter
    private int heartbeatInterval = 20;

    @Getter
    @Setter
    private int rowSize = 15;

    @Getter
    @Setter
    private boolean reversed = false;

    @Getter
    @Setter
    private String defaultTrackerPlaceholder = "---";

    @Getter
    private final UpdateSettings updateSettings = new UpdateSettings();

    @Getter
    private final Set<String> exemptPlayersNames = new LinkedHashSet<>() {
        {
            add("ExamplePlayerName");
        }
    };

    @Getter
    private final Set<String> exemptPlayersUUIDs = new LinkedHashSet<>();
}

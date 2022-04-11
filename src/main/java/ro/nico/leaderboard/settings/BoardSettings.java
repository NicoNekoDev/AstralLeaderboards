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
        this.id = SettingsUtil.getOrSetStringFunction(section, "board-id", this.id,
                Optional.of(List.of("Board id, must be unique")));
        this.sorter = SettingsUtil.getOrSetStringFunction(section, "sorter", this.sorter,
                Optional.of(List.of("Sorter of the board, used for identification.")));
        this.trackers = SettingsUtil.getOrSetSectionFunction(section, "trackers", this.trackers,
                Optional.of(List.of(
                        "A list of trackers to be displayed on placeholders.",
                        "An example tracker is:",
                        "  display: \"%player_name%\"",
                        "Which can be used in the placeholder %astrallb_<board>_<position>_display_<date>%",
                        "where 'display' is the tracker id."
                )));
        this.syncUpdateInterval = SettingsUtil.getOrSetIntFunction(section, "sync-update-interval", this.syncUpdateInterval,
                Optional.of(List.of("Sync update interval in seconds.", "Default is 30 seconds.")));
        this.asyncUpdateInterval = SettingsUtil.getOrSetIntFunction(section, "async-update-interval", this.asyncUpdateInterval,
                Optional.of(List.of("Async update interval in ticks.", "It's being used for multi-threading checks.", "Default is 20 ticks.")));
        this.reversed = SettingsUtil.getOrSetBooleanFunction(section, "reversed", this.reversed,
                Optional.of(List.of("If the board sorting order should be reversed.", "Default is false.")));
        this.defaultTrackerPlaceholder = SettingsUtil.getOrSetStringFunction(section, "default-tracker", this.defaultTrackerPlaceholder,
                Optional.of(List.of("The default tracker placeholder to be used if no tracker is found in the database.", "Default is \"---\".")));
        this.defaultSorterPlaceholder = SettingsUtil.getOrSetStringFunction(section, "default-sorter", this.defaultSorterPlaceholder,
                Optional.of(List.of("The default sorter placeholder to be used when no sorter is found in the database.", "Default is \"---\".")));
        this.updateSettings.load(SettingsUtil.getOrCreateSection(section, "updates", Optional.of(List.of(""))));
        this.exemptPlayers.addAll(SettingsUtil.getOrSetStringCollectionFunction(section, "exempt-players", this.exemptPlayers, Optional.empty()));
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
    private int syncUpdateInterval = 30;

    @Getter
    @Setter
    private int asyncUpdateInterval = 20;

    @Getter
    @Setter
    private boolean reversed = false;

    @Getter
    @Setter
    private String defaultTrackerPlaceholder = "---";

    @Getter
    @Setter
    private String defaultSorterPlaceholder = "---";

    @Getter
    private final UpdateSettings updateSettings = new UpdateSettings();

    @Getter
    private final Set<String> exemptPlayers = new LinkedHashSet<>() {
        {
            add("ExamplePlayerName");
        }
    };
}

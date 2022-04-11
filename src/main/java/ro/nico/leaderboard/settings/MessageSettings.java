package ro.nico.leaderboard.settings;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.configuration.ConfigurationSection;
import ro.nico.leaderboard.util.SettingsUtil;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class MessageSettings implements SettingsSerializer {

    @Override
    public void load(ConfigurationSection section) {
        this.helpMessages.addAll(SettingsUtil.getOrSetStringCollectionFunction(section, "help", this.helpMessages, Optional.empty()));
        this.boardUpdateUsageMessage = SettingsUtil.getOrSetStringFunction(section, "board-update-usage", this.boardUpdateUsageMessage, Optional.empty());
        this.boardCreateUsageMessage = SettingsUtil.getOrSetStringFunction(section, "board-create-usage", this.boardCreateUsageMessage, Optional.empty());
        this.boardDeleteUsageMessage = SettingsUtil.getOrSetStringFunction(section, "board-delete-usage", this.boardDeleteUsageMessage, Optional.empty());
        this.boardDataUsageMessage = SettingsUtil.getOrSetStringFunction(section, "board-data-usage", this.boardDataUsageMessage, Optional.empty());
        this.trackerAddUsageMessage = SettingsUtil.getOrSetStringFunction(section, "tracker-add-usage", this.trackerAddUsageMessage, Optional.empty());
        this.trackerRemoveUsageMessage = SettingsUtil.getOrSetStringFunction(section, "tracker-remove-usage", this.trackerRemoveUsageMessage, Optional.empty());
        this.noPermissionMessage = SettingsUtil.getOrSetStringFunction(section, "no-permission", this.noPermissionMessage, Optional.empty());
        this.reloadSuccessMessage = SettingsUtil.getOrSetStringFunction(section, "reload-success", this.reloadSuccessMessage, Optional.empty());
        this.boardCreateSuccessMessage = SettingsUtil.getOrSetStringFunction(section, "board-create-success", this.boardCreateSuccessMessage, Optional.empty());
        this.boardCreateFailMessage = SettingsUtil.getOrSetStringFunction(section, "board-create-fail", this.boardCreateFailMessage, Optional.empty());
        this.boardDeleteSuccessMessage = SettingsUtil.getOrSetStringFunction(section, "board-delete-success", this.boardDeleteSuccessMessage, Optional.empty());
        this.boardNotFoundMessage = SettingsUtil.getOrSetStringFunction(section, "board-not-found", this.boardNotFoundMessage, Optional.empty());
        this.boardUpdatedMessage = SettingsUtil.getOrSetStringFunction(section, "board-updated", this.boardUpdatedMessage, Optional.empty());
        this.boardsUpdatedMessage = SettingsUtil.getOrSetStringFunction(section, "boards-updated", this.boardsUpdatedMessage, Optional.empty());
        this.boardAlreadyExistsMessage = SettingsUtil.getOrSetStringFunction(section, "board-already-exists", this.boardAlreadyExistsMessage, Optional.empty());
        this.boardInvalidIdMessage = SettingsUtil.getOrSetStringFunction(section, "board-invalid-id", this.boardInvalidIdMessage, Optional.empty());
        this.invalidDateTypeMessage = SettingsUtil.getOrSetStringFunction(section, "invalid-date-type", this.invalidDateTypeMessage, Optional.empty());
        this.dataHeaderMessage = SettingsUtil.getOrSetStringFunction(section, "data-header", this.dataHeaderMessage, Optional.empty());
        this.dataHeaderEntryMessage = SettingsUtil.getOrSetStringFunction(section, "data-header-entry", this.dataHeaderEntryMessage, Optional.empty());
        this.trackerAlreadyExistsMessage = SettingsUtil.getOrSetStringFunction(section, "tracker-already-exists", this.trackerAlreadyExistsMessage, Optional.empty());
        this.trackerNotFoundMessage = SettingsUtil.getOrSetStringFunction(section, "tracker-not-found", this.trackerNotFoundMessage, Optional.empty());
        this.trackerAddedMessage = SettingsUtil.getOrSetStringFunction(section, "tracker-added", this.trackerAddedMessage, Optional.empty());
        this.trackerRemovedMessage = SettingsUtil.getOrSetStringFunction(section, "tracker-removed", this.trackerRemovedMessage, Optional.empty());
        this.boardDateTypeNotEnabledMessage = SettingsUtil.getOrSetStringFunction(section, "board-date-type-not-enabled", this.boardDateTypeNotEnabledMessage, Optional.empty());
    }

    @Getter
    private final List<String> helpMessages = new LinkedList<>() {
        {
            add("&7&m-----------------------------------------------------");
            add("&e/astrallb help &7- &aShows this help.");
            add("&e/astrallb reload &7- &aReloads the config and boards.");
            add("&e/astrallb update <board> &7- &aForces a board update.");
            add("&e/astrallb create <board> <sorter> &7- &aCreates a new board.");
            add("&e/astrallb delete <board> &7- &aDeletes the board.");
            add("&e/astrallb data <board> <type> &7- &aShows the data of the board.");
            add("&e/astrallb trackeradd <board> <tracker> <placeholder> &7- &aAdds a tracker to the board.");
            add("&7&m-----------------------------------------------------");
        }
    };

    @NonNull
    @Getter
    private String boardUpdateUsageMessage = "&cUsage: &e/astrallb update <board>";

    @NonNull
    @Getter
    private String boardCreateUsageMessage = "&cUsage: &e/astrallb create <board> <sorter>";

    @NonNull
    @Getter
    private String boardDeleteUsageMessage = "&cUsage: &e/astrallb delete <board>";

    @NonNull
    @Getter
    private String boardDataUsageMessage = "&cUsage: &e/astrallb data <board> <date> [page]";

    @NonNull
    @Getter
    private String trackerAddUsageMessage = "&cUsage: &e/astrallb addtracker <board> <tracker> <placeholder>";

    @NonNull
    @Getter
    private String trackerRemoveUsageMessage = "&cUsage: &e/astrallb removetracker <board> <tracker>";

    @NonNull
    @Getter
    private String noPermissionMessage = "&cYou don't have permission to do that!";

    @NonNull
    @Getter
    private String reloadSuccessMessage = "&aSuccessfully reloaded the plugin!";

    @NonNull
    @Getter
    private String boardCreateSuccessMessage = "&aSuccessfully created the board!";

    @NonNull
    @Getter
    private String boardCreateFailMessage = "&cError while creating the board! Please check console for more info.";

    @NonNull
    @Getter
    private String boardDeleteSuccessMessage = "&aSuccessfully deleted the board!";

    @NonNull
    @Getter
    private String boardNotFoundMessage = "&cThe board &e%board% &cwas not found!";

    @NonNull
    @Getter
    private String boardUpdatedMessage = "&aSuccessfully updated the board &e%board%&a!";

    @NonNull
    @Getter
    private String boardsUpdatedMessage = "&aSuccessfully updated all boards!";

    @NonNull
    @Getter
    private String boardAlreadyExistsMessage = "&cThe board &e%board% &calready exists!";

    @NonNull
    @Getter
    private String boardInvalidIdMessage = "&cThe id is invalid! It mush only contain letters, numbers and dashes!";

    @NonNull
    @Getter
    private String invalidDateTypeMessage = "&cThe date is invalid! It must be one of the following: &e%date_types%";

    @NonNull
    @Getter
    private String dataHeaderMessage = "&e%board% &7- &aShowing page %page%";

    @NonNull
    @Getter
    private String dataHeaderEntryMessage = "&e%rank%) &a%name% &7- &a%sorter% &7(&b%trackers%&7)";

    @NonNull
    @Getter
    private String trackerAlreadyExistsMessage = "&cThe tracker &e%tracker% &calready exists!";

    @NonNull
    @Getter
    private String trackerNotFoundMessage = "&cThe tracker &e%tracker% &cwas not found!";

    @NonNull
    @Getter
    private String trackerAddedMessage = "&aSuccessfully added the tracker &e%tracker%&a!";

    @NonNull
    @Getter
    private String trackerRemovedMessage = "&aSuccessfully removed the tracker &e%tracker%&a!";

    @NonNull
    @Getter
    private String boardDateTypeNotEnabledMessage = "&cThe date type &e%type% &cis not enabled for the board &e%board%&c!";
}

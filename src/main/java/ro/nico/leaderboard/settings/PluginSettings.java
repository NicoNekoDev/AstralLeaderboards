package ro.nico.leaderboard.settings;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.NonNull;

import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("FieldMayBeFinal")
public class PluginSettings {

    @NonNull
    @Getter
    @SerializedName("default-placeholder")
    private String defaultPlaceholder = "---";

    @NonNull
    @Getter
    @SerializedName("storage")
    private StorageSettings storageSettings = new StorageSettings();

    @NonNull
    @Getter
    @SerializedName("messages")
    private MessageSettings messageSettings = new MessageSettings();


    public static class StorageSettings {
        @Getter
        @SerializedName("use-mysql")
        private final boolean usingMySQL = false;

        @Getter
        @SerializedName("mysql")
        private final StorageMySQLSettings MySQLSettings = new StorageMySQLSettings();

        @Getter
        @SerializedName("sqlite")
        private final StorageSQLiteSettings SQLiteSettings = new StorageSQLiteSettings();

        @SuppressWarnings("FieldMayBeFinal")
        public static class StorageMySQLSettings {
            @Getter private String host = "localhost";
            @Getter private int port = 3306;
            @Getter private String database = "leaderboard";
            @Getter private String username = "root";
            @Getter private String password = "";
            @Getter @SerializedName("ssl_enabled") private boolean SSLEnabled = false;
            @Getter @SerializedName("table_prefix") private String tablePrefix = "astrallb_";
        }

        public static class StorageSQLiteSettings {
            @Getter
            @SerializedName("file-name")
            private final String fileName = "leaderboard.db";
        }
    }

    @SuppressWarnings("FieldMayBeFinal")
    public static class MessageSettings {
        @Getter
        @SerializedName("help")
        private List<String> helpMessages = new LinkedList<>() {
            {
                add("&7&m-----------------------------------------------------");
                add("&e/astrallb help &7- &aShows this help.");
                add("&e/astrallb reload &7- &aReloads the config and boards.");
                add("&e/astrallb update <board> &7- &aForces a board update.");
                add("&e/astrallb create <board> <sorter> &7- &aCreates a new board.");
                add("&e/astrallb delete <board> &7- &aDeletes the board.");
                add("&e/astrallb data <board> <type> &7- &aShows the data of the board.");
                add("&7&m-----------------------------------------------------");
            }
        };

        @NonNull
        @Getter
        @SerializedName("board-update-usage")
        private String boardUpdateUsageMessage = "&cUsage: &e/astrallb update <board>";

        @NonNull
        @Getter
        @SerializedName("board-create-usage")
        private String boardCreateUsageMessage = "&cUsage: &e/astrallb create <board> <sorter>";

        @NonNull
        @Getter
        @SerializedName("board-delete-usage")
        private String boardDeleteUsageMessage = "&cUsage: &e/astrallb delete <board>";

        @NonNull
        @Getter
        @SerializedName("board-data-usage")
        private String boardDataUsageMessage = "&cUsage: &e/astrallb data <board> <date>";

        @NonNull
        @Getter
        @SerializedName("no-permission")
        private String noPermissionMessage = "&cYou don't have permission to do that!";

        @NonNull
        @Getter
        @SerializedName("reload-success")
        private String reloadSuccessMessage = "&aSuccessfully reloaded the plugin!";

        @NonNull
        @Getter
        @SerializedName("board-create-success")
        private String boardCreateSuccessMessage = "&aSuccessfully created the board!";

        @NonNull
        @Getter
        @SerializedName("board-create-fail")
        private String boardCreateErrorMessage = "&cError while creating the board! Please check console for more info.";

        @NonNull
        @Getter
        @SerializedName("board-delete-success")
        private String boardDeleteSuccessMessage = "&aSuccessfully deleted the board!";

        @NonNull
        @Getter
        @SerializedName("board-not-found")
        private String boardNotFoundMessage = "&cThe board &e%board% &cwas not found!";

        @NonNull
        @Getter
        @SerializedName("board-updated")
        private String boardUpdatedMessage = "&aSuccessfully updated the board &e%board%&a!";

        @NonNull
        @Getter
        @SerializedName("board-already-exists")
        private String boardAlreadyExistsMessage = "&cThe board &e%board% &calready exists!";

        @NonNull
        @Getter
        @SerializedName("board-id-invalid")
        private String boardInvalidIdMessage = "&cThe id is invalid! It mush only contain letters, numbers and dashes!";

        @NonNull
        @Getter
        @SerializedName("date-type-invalid")
        private String invalidDateTypeMessage = "&cThe date is invalid! It must be one of the following: &e%date_types%";

        @NonNull
        @Getter
        @SerializedName("data-header")
        private String boardDataHeaderMessage = "&e%board% &7- &aShowing %size% entries";

        @NonNull
        @Getter
        @SerializedName("data-header-entry")
        private String boardDataEntryMessage = "&e%rank%) &a%name% &7- &a%sorter% &7(&b%trackers%&7)";
    }
}

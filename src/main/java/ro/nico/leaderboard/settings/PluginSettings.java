package ro.nico.leaderboard.settings;

import com.mrivanplays.annotationconfig.core.annotations.Key;
import com.mrivanplays.annotationconfig.core.annotations.comment.Comment;
import lombok.Getter;
import lombok.NonNull;

import java.util.List;

public class PluginSettings {

    @NonNull
    @Getter
    @Comment("The default placeholder used when the data was not found")
    private final String defaultPlaceholder = "???";

    @Getter
    @Comment("If the plugin should use MySQL or SQLite")
    @Key("storage.use_mysql")
    private final boolean usingMySQL = false;

    @NonNull
    @Getter
    @Key("storage.mysql.user")
    private final String MySQLUser = "root";

    @NonNull
    @Getter
    @Key("storage.mysql.password")
    private final String MySQLPassword = "";

    @NonNull
    @Getter
    @Key("storage.mysql.host")
    private final String MySQLHost = "localhost";

    @NonNull
    @Getter
    @Key("storage.mysql.port")
    private final String MySQLPort = "3306";

    @NonNull
    @Getter
    @Key("storage.mysql.database")
    private final String MySQLDatabase = "leaderboard";

    @Getter
    @Key("storage.mysql.ssl_enabled")
    private final boolean MySQLSSLEnabled = false;

    @NonNull
    @Getter
    @Key("storage.mysql.table_prefix")
    private final String MySQLTablePrefix = "astrallb_";

    @NonNull
    @Getter
    @Key("storage.sqlite.file_name")
    private final String SQLiteFileName = "leaderboard.db";

    @NonNull
    @Getter
    @Key("messages.help")
    private final List<String> helpMessages = List.of(
            "&a&m====================",
            "&a&l&oAstralLeaderboards",
            "",
            "&e/astrallb help &7- &aShows this help.",
            "&e/astrallb reload &7- &aReloads the config and boards.",
            "&e/astrallb update <board> &7- &aForces a board update.",
            "&e/astrallb create <board> <sorter> &7- &aCreates a new board.",
            "&e/astrallb delete <board> &7- &aDeletes the board.",
            "&e/astrallb data <board> <type> &7- &aShows the data of the board.",
            "",
            "&a&m====================");

    @NonNull
    @Getter
    @Key("messages.board-update-usage")
    private final String boardUpdateUsageMessage = "&cUsage: &e/astrallb update <board>";

    @NonNull
    @Getter
    @Key("messages.board-create-usage")
    private final String boardCreateUsageMessage = "&cUsage: &e/astrallb create <board> <sorter>";

    @NonNull
    @Getter
    @Key("messages.board-update-usage")
    private final String boardDeleteUsageMessage = "&cUsage: &e/astrallb delete <board>";

    @NonNull
    @Getter
    @Key("message.board-data-usage")
    private final String boardDataUsageMessage = "&cUsage: &e/astrallb data <board> <date>";

    @NonNull
    @Getter
    @Key("messages.no-permission")
    private final String noPermissionMessage = "&cYou don't have permission to do that!";

    @NonNull
    @Getter
    @Key("messages.reload-success")
    private final String reloadSuccessMessage = "&aSuccessfully reloaded the plugin!";

    @NonNull
    @Getter
    @Key("messages.board-create-success")
    private final String boardCreateSuccessMessage = "&aSuccessfully created the board!";

    @NonNull
    @Getter
    @Key("messages.board-delete-success")
    private final String boardDeleteSuccessMessage = "&aSuccessfully deleted the board!";

    @NonNull
    @Getter
    @Key("messages.board-not-found")
    private final String boardNotFoundMessage = "&cThe board &e%board% &cwas not found!";

    @NonNull
    @Getter
    @Key("messages.board-updated")
    private final String boardUpdatedMessage = "&aSuccessfully updated the board &e%board%&a!";

    @NonNull
    @Getter
    @Key("messages.board-already-exists")
    private final String boardAlreadyExistsMessage = "&cThe board &e%board% &calready exists!";

    @NonNull
    @Getter
    @Key("messages.board-id-invalid")
    private final String boardInvalidIdMessage = "&cThe id is invalid! It mush only contain letters, numbers and dashes!";

    @NonNull
    @Getter
    @Key("messages.date-type-invalid")
    private final String invalidDateTypeMessage = "&cThe date is invalid! It must be one of the following: &e%date_types%";

    @NonNull
    @Getter
    @Key("messages.data-header")
    private final String boardDataHeaderMessage = "&e%board% &7- &aShowing %size% entries";

    @NonNull
    @Getter
    @Key("messages.data-header-entry")
    private final String boardDataEntryMessage = "&e%rank%) &a%name% &7- &a%sorter% &7(&b%trackers%&7)";
}

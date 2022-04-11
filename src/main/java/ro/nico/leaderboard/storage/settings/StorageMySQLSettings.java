package ro.nico.leaderboard.storage.settings;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import ro.nico.leaderboard.settings.SettingsSerializer;
import ro.nico.leaderboard.util.SettingsUtil;

import java.util.List;
import java.util.Optional;

public class StorageMySQLSettings implements SettingsSerializer {
    @Getter private String host = "localhost";
    @Getter private int port = 3306;
    @Getter private String database = "leaderboard";
    @Getter private String username = "root";
    @Getter private String password = "";
    @Getter private boolean SSLEnabled = false;
    @Getter private String tablePrefix = "astrallb_";

    @Override
    public void load(ConfigurationSection section) {
        this.host = SettingsUtil.getOrSetStringFunction(section, "host", this.host, Optional.empty());
        this.port = SettingsUtil.getOrSetIntFunction(section, "port", this.port, Optional.empty());
        this.database = SettingsUtil.getOrSetStringFunction(section, "database", this.database, Optional.empty());
        this.username = SettingsUtil.getOrSetStringFunction(section, "username", this.username, Optional.empty());
        this.password = SettingsUtil.getOrSetStringFunction(section, "password", this.password, Optional.empty());
        this.SSLEnabled = SettingsUtil.getOrSetBooleanFunction(section, "ssl_enabled", this.SSLEnabled, Optional.of(List.of("If SSL encryption is enabled.")));
        this.tablePrefix = SettingsUtil.getOrSetStringFunction(section, "table_prefix", this.tablePrefix, Optional.of(List.of("The prefix for the table name.")));
    }
}

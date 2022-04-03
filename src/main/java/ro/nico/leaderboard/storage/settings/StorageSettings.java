package ro.nico.leaderboard.storage.settings;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import ro.nico.leaderboard.settings.SettingsSerializer;
import ro.nico.leaderboard.util.SettingsUtil;

import java.util.List;
import java.util.Optional;

public class StorageSettings implements SettingsSerializer {
    @Getter
    private boolean usingMySQL = false;

    @Getter
    private final StorageMySQLSettings MySQLSettings = new StorageMySQLSettings();

    @Getter
    private final StorageSQLiteSettings SQLiteSettings = new StorageSQLiteSettings();

    @Override
    public void load(ConfigurationSection section) {
        this.usingMySQL = SettingsUtil.getOrSetBooleanFunction().apply(section, "use-mysql", this.usingMySQL, Optional.of(List.of("If MySQL is gonna be used.")));
        this.MySQLSettings.load(SettingsUtil.getOrCreateSection().apply(section, "mysql", Optional.empty()));
        this.SQLiteSettings.load(SettingsUtil.getOrCreateSection().apply(section, "sqlite", Optional.empty()));
    }
}

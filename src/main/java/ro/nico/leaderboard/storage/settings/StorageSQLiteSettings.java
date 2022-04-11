package ro.nico.leaderboard.storage.settings;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import ro.nico.leaderboard.settings.SettingsSerializer;
import ro.nico.leaderboard.util.SettingsUtil;

import java.util.Optional;

public class StorageSQLiteSettings implements SettingsSerializer {
    @Getter
    private String fileName = "leaderboard.db";

    @Override
    public void load(ConfigurationSection section) {
        this.fileName = SettingsUtil.getOrSetStringFunction(section, "file-name", this.fileName, Optional.empty());
    }
}

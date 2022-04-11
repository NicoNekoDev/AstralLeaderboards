package ro.nico.leaderboard.settings;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.configuration.ConfigurationSection;
import ro.nico.leaderboard.storage.settings.StorageSettings;
import ro.nico.leaderboard.util.SettingsUtil;

import java.util.Optional;

public class PluginSettings implements SettingsSerializer {

    @Override
    public void load(ConfigurationSection section) {
        this.defaultPlaceholder = SettingsUtil.getOrSetStringFunction(section, "default-placeholder", this.defaultPlaceholder, Optional.empty());
        this.storageSettings.load(SettingsUtil.getOrCreateSection(section, "storage", Optional.empty()));
        this.messageSettings.load(SettingsUtil.getOrCreateSection(section, "messages", Optional.empty()));
    }

    @NonNull
    @Getter
    private String defaultPlaceholder = "???";

    @NonNull
    @Getter
    private final StorageSettings storageSettings = new StorageSettings();

    @NonNull
    @Getter
    private final MessageSettings messageSettings = new MessageSettings();
}

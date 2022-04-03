package ro.nico.leaderboard.settings;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public interface SettingsSerializer {
    void load(ConfigurationSection section);
}

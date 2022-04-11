package ro.nico.leaderboard.settings;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;
import ro.nico.leaderboard.util.SettingsUtil;

import java.util.List;
import java.util.Optional;

public class UpdateSettings implements SettingsSerializer {

    @Override
    public void load(ConfigurationSection section) {
        this.hourlyUpdated = SettingsUtil.getOrSetBooleanFunction(section, "hourly", this.hourlyUpdated,
                Optional.of(List.of("If the board should be updated hourly.", "Default is false.")));
        this.dailyUpdated = SettingsUtil.getOrSetBooleanFunction(section, "daily", this.dailyUpdated,
                Optional.of(List.of("If the board should be updated daily.", "Default is false.")));
        this.weeklyUpdated = SettingsUtil.getOrSetBooleanFunction(section, "weekly", this.weeklyUpdated,
                Optional.of(List.of("If the board should be updated weekly.", "Default is false.")));
        this.monthlyUpdated = SettingsUtil.getOrSetBooleanFunction(section, "monthly", this.monthlyUpdated,
                Optional.of(List.of("If the board should be updated monthly.", "Default is false.")));
        this.yearlyUpdated = SettingsUtil.getOrSetBooleanFunction(section, "yearly", this.yearlyUpdated,
                Optional.of(List.of("If the board should be updated yearly.", "Default is false.")));
    }

    @Getter
    @Setter
    private boolean hourlyUpdated = false;

    @Getter
    @Setter
    private boolean dailyUpdated = false;

    @Getter
    @Setter
    private boolean weeklyUpdated = false;

    @Getter
    @Setter
    private boolean monthlyUpdated = false;

    @Getter
    @Setter
    private boolean yearlyUpdated = false;
}

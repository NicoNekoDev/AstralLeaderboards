package ro.nico.leaderboard.util;

import io.github.NicoNekoDev.SimpleTuples.func.QuartetFunction;
import io.github.NicoNekoDev.SimpleTuples.func.TripletFunction;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class SettingsUtil {

    public static TripletFunction<ConfigurationSection, String, Optional<List<String>>, ConfigurationSection> getOrCreateSection() {
        return (config, path, comments) -> {
            ConfigurationSection value = config.isConfigurationSection(path) ? config.getConfigurationSection(path) : config.createSection(path);
            comments.ifPresent(strings -> config.setComments(path, strings));
            return value;
        };
    }

    public static QuartetFunction<ConfigurationSection, String, String, Optional<List<String>>, String> getOrSetStringFunction() {
        return (config, path, defaultValue, comments) -> {
            String value = defaultValue;
            if (config.isString(path)) {
                value = config.getString(path);
            } else {
                config.set(path, value);
                comments.ifPresent(strings -> config.setComments(path, strings));
            }
            return value;
        };
    }

    public static QuartetFunction<ConfigurationSection, String, Integer, Optional<List<String>>, Integer> getOrSetIntFunction() {
        return (config, path, defaultValue, comments) -> {
            Integer value = defaultValue;
            if (config.isInt(path)) {
                value = config.getInt(path);
            } else {
                config.set(path, value);
                comments.ifPresent(strings -> config.setComments(path, strings));
            }
            return value;
        };
    }

    public static QuartetFunction<ConfigurationSection, String, ConfigurationSection, Optional<List<String>>, ConfigurationSection> getOrSetSectionFunction() {
        return (config, path, defaultValue, comments) -> {
            ConfigurationSection value = defaultValue;
            if (config.isConfigurationSection(path)) {
                value = config.getConfigurationSection(path);
            } else {
                config.set(path, value);
                comments.ifPresent(strings -> config.setComments(path, strings));
            }
            return value;
        };
    }

    public static QuartetFunction<ConfigurationSection, String, Boolean, Optional<List<String>>, Boolean> getOrSetBooleanFunction() {
        return (config, path, defaultValue, comments) -> {
            boolean value = defaultValue;
            if (config.isBoolean(path)) {
                value = config.getBoolean(path);
            } else {
                config.set(path, value);
                comments.ifPresent(strings -> config.setComments(path, strings));
            }
            return value;
        };
    }

    public static QuartetFunction<ConfigurationSection, String, Collection<String>, Optional<List<String>>, Collection<String>> getOrSetStringCollectionFunction() {
        return (config, path, defaultValue, comments) -> {
            List<String> value = new ArrayList<>(defaultValue);
            if (config.isList(path)) {
                value = config.getStringList(path);
            } else {
                config.set(path, value);
                comments.ifPresent(strings -> config.setComments(path, strings));
            }
            defaultValue.clear();
            return value;
        };
    }
}

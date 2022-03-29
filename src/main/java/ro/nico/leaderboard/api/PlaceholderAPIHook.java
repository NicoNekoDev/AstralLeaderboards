package ro.nico.leaderboard.api;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.nico.leaderboard.AstralLeaderboardsPlugin;
import ro.nico.leaderboard.storage.SQLDateType;
import ro.nico.leaderboard.storage.cache.PlayerData;

public class PlaceholderAPIHook extends PlaceholderExpansion {
    private final AstralLeaderboardsPlugin plugin;

    public PlaceholderAPIHook(AstralLeaderboardsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "astrallb";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Nico";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    // %astrallb_<board>_<position>_<tracker>_<date>%
    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        String defaultValue = plugin.getConfig().getString("default-placeholder", "???");
        String[] args = params.split("_");
        if (args.length == 4) {
            String boardData = args[0];
            int position;
            try {
                position = Integer.parseInt(args[1]);
                position--;
                if (position < 0)
                    return defaultValue;
            } catch (NumberFormatException e) {
                return defaultValue;
            }
            String tracker = args[2];
            SQLDateType time;
            try {
                time = SQLDateType.valueOf(args[3].toUpperCase());
            } catch (IllegalArgumentException e) {
                return defaultValue;
            }
            Board board = plugin.getBoardsManager().getBoard(boardData);
            if (board == null)
                return defaultValue;
            try {
                PlayerData data = board.getBoardData().getData(position, time);
                if (data == null)
                    return board.getBoardSettings().getDefaultTrackerPlaceholder();
                String trackerData = data.getTrackers().get(tracker);
                return trackerData == null ? board.getBoardSettings().getDefaultTrackerPlaceholder() : trackerData;
            } catch (IndexOutOfBoundsException ex) {
                return board.getBoardSettings().getDefaultTrackerPlaceholder();
            }
        }
        return defaultValue;
    }
}

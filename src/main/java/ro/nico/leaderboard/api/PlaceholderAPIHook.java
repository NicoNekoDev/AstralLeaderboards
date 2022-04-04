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

    // %astrallb_<board>_<tracker>_<position>_<date>%
    // example: %astrallb_money_display_1_alltime%
    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        String defaultValue = plugin.getSettings().getDefaultPlaceholder();
        String[] args = params.split("_");
        if (args.length == 4) {
            String board = args[0];
            String tracker = args[1];
            int position;
            try {
                position = Integer.parseInt(args[2]);
                if (position < 1)
                    return defaultValue;
            } catch (NumberFormatException e) {
                return defaultValue;
            }
            SQLDateType time;
            try {
                time = SQLDateType.valueOf(args[3].toUpperCase());
            } catch (IllegalArgumentException e) {
                return defaultValue;
            }
            Board boardData = plugin.getBoardsManager().getBoard(board);
            if (boardData == null)
                return defaultValue;
            try {
                PlayerData data = boardData.getBoardData().getData(--position, time);
                if (data == null)
                    return boardData.getBoardSettings().getDefaultTrackerPlaceholder();
                String trackerData = data.getTrackers().get(tracker);
                return trackerData == null ? boardData.getBoardSettings().getDefaultTrackerPlaceholder() : trackerData;
            } catch (IndexOutOfBoundsException ex) {
                return boardData.getBoardSettings().getDefaultTrackerPlaceholder();
            }
        }
        return defaultValue;
    }
}

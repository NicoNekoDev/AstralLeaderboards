package ro.nico.leaderboard.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import ro.nico.leaderboard.AstralLeaderboardsPlugin;

public class PlayerEvents implements Listener {
    private final AstralLeaderboardsPlugin plugin;

    public PlayerEvents(AstralLeaderboardsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getBoardsManager().getBoards().forEach((id, board) -> board.getBoardData().updatePlayerDataImmediately(event.getPlayer()));
    }
}

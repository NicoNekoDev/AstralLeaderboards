package ro.nico.leaderboard.api.event;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import ro.nico.leaderboard.api.Board;

public class BoardDeleteEvent extends Event {
    public static final HandlerList handlers = new HandlerList();
    @Getter private final Board board;

    public BoardDeleteEvent(@NotNull Board board) {
        this.board = board;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}

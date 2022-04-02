package ro.nico.leaderboard.api;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.NotNull;
import ro.nico.leaderboard.AstralLeaderboardsPlugin;
import ro.nico.leaderboard.settings.BoardSettings;
import ro.nico.leaderboard.util.GsonUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class BoardsManager {
    private final AstralLeaderboardsPlugin plugin;
    private final Map<String, Board> boards = new HashMap<>();
    private final File boardsDirectory;
    public static final Pattern BOARD_ID_PATTERN = Pattern.compile("[a-zA-Z0-9-]+");

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public BoardsManager(@NotNull AstralLeaderboardsPlugin plugin) {
        this.plugin = plugin;
        this.boardsDirectory = new File(plugin.getDataFolder(), "boards");
        this.boardsDirectory.mkdirs();
    }

    public void loadAllBoards() {
        FileVisitor<Path> fileVisitor = new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
                if (path.toString().endsWith(".json")) {
                    BoardSettings settings;
                    try {
                        settings = GsonUtil.load(BoardSettings.class, path.toFile());
                    } catch (IOException e) {
                        plugin.getLogger().warning("Failed to load board settings from " + path);
                        return FileVisitResult.CONTINUE;
                    }
                    String id = settings.getId();
                    if (!BOARD_ID_PATTERN.matcher(id).matches()) {
                        plugin.getLogger().warning("Invalid board id: " + id);
                        return FileVisitResult.CONTINUE;
                    }
                    if (boards.containsKey(id)) {
                        plugin.getLogger().warning("Duplicate board id: " + id);
                        return FileVisitResult.CONTINUE;
                    }
                    Board board = new Board(plugin, id, path.toFile(), settings);
                    BoardsManager.this.boards.put(id, board);
                }
                return FileVisitResult.CONTINUE;
            }
        };
        try {
            Files.walkFileTree(this.boardsDirectory.toPath(), fileVisitor);
            this.plugin.getLogger().info("Loaded " + this.boards.size() + " boards");
        } catch (IOException ex) {
            this.plugin.getLogger().warning("Failed to load boards: " + ex.getMessage());
        }
        this.boards.forEach((name, board) -> board.enable());
    }

    public void unloadAllBoards() {
        this.boards.forEach((name, board) -> board.disable());
        this.boards.clear();
    }

    public Board createBoard(@NotNull String id, @NotNull String sorter) throws IllegalArgumentException, IOException {
        if (this.hasBoard(id))
            throw new IllegalArgumentException("Board with id " + id + " already exists");
        if (!BOARD_ID_PATTERN.matcher(id).matches())
            throw new IllegalArgumentException("Invalid board id pattern: " + id);
        BoardSettings boardSettings = new BoardSettings(id, sorter);
        File boardFile = new File(this.boardsDirectory, id + ".json");
        boardSettings = GsonUtil.fromOrToJson(boardSettings, BoardSettings.class, boardFile); // also check if a config file with the same board id exists, and load it
        Board board = new Board(this.plugin, id, boardFile, boardSettings);
        this.boards.put(id, board);
        return board;
    }

    public void deleteBoard(@NotNull String id) {
        Board board = this.boards.remove(id);
        if (board != null) {
            board.disable();
            board.deleteSettings();
        }
    }

    public Map<String, Board> getBoards() {
        return ImmutableMap.copyOf(this.boards);
    }

    public Set<String> getBoardsIds() {
        return ImmutableSet.copyOf(this.boards.keySet());
    }

    public Board getBoard(@NotNull String id) {
        return this.boards.get(id);
    }

    public void removeBoard(@NotNull String id) {
        Board board = this.boards.remove(id);
        if (board != null)
            board.disable();
    }

    public boolean hasBoard(@NotNull String name) {
        return this.boards.containsKey(name);
    }
}

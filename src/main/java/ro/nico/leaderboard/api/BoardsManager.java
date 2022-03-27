package ro.nico.leaderboard.api;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import ro.nico.leaderboard.AstralLeaderboardsPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BoardsManager {
    private final AstralLeaderboardsPlugin plugin;
    private final Map<String, Board> boards = new HashMap<>();
    private final File boardsDirectory;

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
                if (path.toString().endsWith(".yml")) {
                    YamlConfiguration boardConfig = YamlConfiguration.loadConfiguration(path.toFile());
                    if (!boardConfig.isString("id")) {
                        plugin.getLogger().warning("Invalid board id in board config file: " + path);
                        return FileVisitResult.CONTINUE;
                    }
                    if (!boardConfig.isString("sorter")) {
                        plugin.getLogger().warning("Invalid board sorter in board config file: " + path);
                        return FileVisitResult.CONTINUE;
                    }
                    String id = boardConfig.getString("id", "default");
                    if (boards.containsKey(id)) {
                        plugin.getLogger().warning("Duplicate board id: " + id);
                        return FileVisitResult.CONTINUE;
                    }
                    Board board = new Board(plugin, id, path.toFile(), boardConfig);
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

    public Board createBoard(@NotNull String id, @NotNull String sorter) {
        if (this.hasBoard(id))
            throw new IllegalArgumentException("Board with id " + id + " already exists");
        YamlConfiguration boardConfig = new YamlConfiguration();
        boardConfig.set("id", id);
        boardConfig.set("sorter", sorter);
        File boardFile = new File(this.boardsDirectory, id + ".yml");
        try {
            boardConfig.save(boardFile);
            Board board = new Board(this.plugin, id, boardFile, boardConfig);
            this.boards.put(id, board);
            return board;
        } catch (IOException e) {
            this.plugin.getLogger().warning("Failed to create board config file: " + e.getMessage());
        }
        return null;
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

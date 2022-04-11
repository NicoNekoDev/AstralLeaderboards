package ro.nico.leaderboard.api;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import ro.nico.leaderboard.AstralLeaderboardsPlugin;
import ro.nico.leaderboard.api.event.BoardCreateEvent;
import ro.nico.leaderboard.api.event.BoardDeleteEvent;
import ro.nico.leaderboard.settings.BoardSettings;

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
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(path.toFile());
                    BoardSettings settings = new BoardSettings();
                    settings.load(config);
                    String id = settings.getId();
                    if (!BOARD_ID_PATTERN.matcher(id).matches()) {
                        plugin.getLogger().warning("Invalid board id: " + id);
                        return FileVisitResult.CONTINUE;
                    }
                    if (boards.containsKey(id)) {
                        plugin.getLogger().warning("Duplicate board id: " + id);
                        return FileVisitResult.CONTINUE;
                    }
                    try {
                        config.save(path.toFile());
                    } catch (IOException e) {
                        plugin.getLogger().warning("Missing board config: " + id);
                        return FileVisitResult.CONTINUE;
                    }
                    Board board = new Board(plugin, id, path.toFile(), settings);
                    try {
                        board.enable();
                    } catch (IOException e) {
                        plugin.getLogger().warning("Failed to enable board: " + id);
                        return FileVisitResult.CONTINUE;
                    }
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
    }

    public void unloadAllBoards() {
        this.boards.forEach((name, board) -> board.disable());
        this.boards.clear();
    }

    public Board createBoard(@NotNull String id, @NotNull String sorter) throws IllegalArgumentException, IOException, InvalidConfigurationException {
        if (this.hasBoard(id))
            throw new IllegalArgumentException("Board with id " + id + " already exists");
        if (!BOARD_ID_PATTERN.matcher(id).matches())
            throw new IllegalArgumentException("Invalid board id pattern: " + id);
        File boardFile = new File(this.boardsDirectory, id + ".yml");
        BoardSettings boardSettings = new BoardSettings(id, sorter);
        Board board = new Board(this.plugin, id, boardFile, boardSettings);
        board.loadSettings();
        this.boards.put(id, board);
        board.saveSettings();
        board.enable();
        Bukkit.getPluginManager().callEvent(new BoardCreateEvent(board));
        return board;
    }

    public void deleteBoard(@NotNull String id) {
        Board board = this.boards.remove(id);
        if (board != null) {
            board.disable();
            board.deleteSettings();
            Bukkit.getPluginManager().callEvent(new BoardDeleteEvent(board));
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

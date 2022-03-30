package ro.nico.leaderboard;

import com.google.common.collect.ImmutableMap;
import io.github.NicoNekoDev.SimpleTuples.Pair;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.nico.leaderboard.api.Board;
import ro.nico.leaderboard.api.BoardsManager;
import ro.nico.leaderboard.storage.SQLDateType;
import ro.nico.leaderboard.storage.cache.PlayerData;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class AstralLeaderboardsCommand implements TabExecutor {
    private final AstralLeaderboardsPlugin plugin;

    public AstralLeaderboardsCommand(AstralLeaderboardsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 0) {
            if (plugin.getVaultPermissions().has(sender, "astralleaderboards.command.help"))
                this.sendHelp(sender);
            else
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getSettings().getNoPermissionMessage()));
        }
        switch (args[0].toLowerCase()) {
            case "help" -> {
                if (plugin.getVaultPermissions().has(sender, "astralleaderboards.command.help"))
                    this.sendHelp(sender);
                else
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getSettings().getNoPermissionMessage()));
            }
            case "reload" -> {
                if (!plugin.getVaultPermissions().has(sender, "astralleaderboards.command.reload")) {
                    plugin.reloadPlugin();
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getSettings().getReloadSuccessMessage()));
                } else
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getSettings().getNoPermissionMessage()));
            }
            case "update" -> {
                if (plugin.getVaultPermissions().has(sender, "astralleaderboards.command.update")) {
                    if (args.length == 2) {
                        Board board = plugin.getBoardsManager().getBoard(args[1]);
                        if (board == null) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getSettings().getBoardNotFoundMessage().replace("%board%", args[1])));
                        } else {
                            board.getBoardData().update();
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getSettings().getBoardUpdatedMessage().replace("%board%", args[1])));
                        }
                    } else
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getSettings().getBoardUpdateUsageMessage()));
                } else
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getSettings().getNoPermissionMessage()));
            }
            case "create" -> {
                if (plugin.getVaultPermissions().has(sender, "astralleaderboards.command.create")) {
                    if (args.length == 3) {
                        BoardsManager boardsManager = plugin.getBoardsManager();
                        if (boardsManager.hasBoard(args[1])) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getSettings().getBoardAlreadyExistsMessage().replace("%board%", args[1])));
                        } else {
                            if (!BoardsManager.BOARD_ID_PATTERN.matcher(args[1]).matches()) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getSettings().getBoardInvalidIdMessage().replace("%board%", args[1])));
                            } else {
                                plugin.getBoardsManager().createBoard(args[1], args[2]);
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getSettings().getBoardCreateSuccessMessage().replace("%board%", args[1])));
                            }
                        }
                    } else
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getSettings().getBoardCreateUsageMessage()));
                } else
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getSettings().getNoPermissionMessage()));
            }
            case "delete" -> {
                if (plugin.getVaultPermissions().has(sender, "astralleaderboards.command.delete")) {
                    if (args.length == 2) {
                        BoardsManager boardsManager = plugin.getBoardsManager();
                        if (!boardsManager.hasBoard(args[1])) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getSettings().getBoardNotFoundMessage().replace("%board%", args[1])));
                        } else {
                            boardsManager.deleteBoard(args[1]);
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getSettings().getBoardDeleteSuccessMessage().replace("%board%", args[1])));
                        }
                    } else
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getSettings().getBoardCreateUsageMessage()));
                } else
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getSettings().getNoPermissionMessage()));
            }
            case "data" -> {
                if (plugin.getVaultPermissions().has(sender, "astralleaderboards.command.data")) {
                    if (args.length == 3) {
                        Board board = plugin.getBoardsManager().getBoard(args[1]);
                        if (board == null) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getSettings().getBoardNotFoundMessage().replace("%board%", args[1])));
                        } else {
                            try {
                                SQLDateType dateType = SQLDateType.valueOf(args[2].toUpperCase());
                                ImmutableMap<Pair<String, UUID>, PlayerData> data = board.getBoardData().dumpAllData(dateType);
                                for (Map.Entry<Pair<String, UUID>, PlayerData> entry : data.entrySet()) {
                                    Pair<String, UUID> key = entry.getKey();
                                    PlayerData value = entry.getValue();
                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getSettings().getBoardDataHeaderMessage()
                                            .replace("%board%", board.getId())
                                            .replace("%size%", String.valueOf(board.getBoardSettings().getRowSize()))
                                    ));
                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getSettings().getBoardDataEntryMessage()
                                            .replace("%name%", key.getFirstValue())
                                            .replace("%uuid%", key.getSecondValue().toString())
                                            .replace("%sorter-value%", value.getSorter())
                                            .replace("%rank%", String.valueOf(value.getRank()))
                                            .replace("%trackers%", this.trackersToString(value.getTrackers()))
                                    ));
                                }
                            } catch (IllegalArgumentException e) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getSettings().getInvalidDateTypeMessage().replace("%type%", args[2])
                                        .replace("%date_types%", Arrays.stream(SQLDateType.values()).map(type -> type.name().toLowerCase()).collect(Collectors.joining(", ")))));
                            }
                        }
                    } else {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getSettings().getBoardDataUsageMessage()));
                    }
                } else
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getSettings().getNoPermissionMessage()));
            }
        }
        return true;
    }

    private String trackersToString(Map<String, String> trackers) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : trackers.entrySet()) {
            builder.append(entry.getKey()).append(": ").append(entry.getValue()).append(", ");
        }
        return builder.toString();
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 0) {
            return List.of("help", "reload", "update", "create", "delete", "data");
        } else if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], List.of("help", "reload", "update", "create"), List.of());
        } else if (args.length == 2) {
            if ("update".equalsIgnoreCase(args[0]) || "delete".equalsIgnoreCase(args[0]) || "data".equalsIgnoreCase(args[0])) {
                return StringUtil.copyPartialMatches(args[1], plugin.getBoardsManager().getBoardsIds(), List.of());
            }
        } else if (args.length == 3) {
            if ("create".equalsIgnoreCase(args[0])) {
                if (args[2].startsWith("%")) {
                    return StringUtil.copyPartialMatches(args[2], PlaceholderAPI.getRegisteredIdentifiers().stream().map(str -> "%" + str).toList(), List.of());
                }
            } else if ("data".equalsIgnoreCase(args[0])) {
                return StringUtil.copyPartialMatches(args[2], Arrays.stream(SQLDateType.values()).map(type -> type.name().toLowerCase()).toList(), List.of());
            }
        }
        return List.of();
    }

    private void sendHelp(CommandSender sender) {
        for (String str : plugin.getSettings().getHelpMessages()) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', str));
        }
    }
}

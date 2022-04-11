package ro.nico.leaderboard;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.nico.leaderboard.api.Board;
import ro.nico.leaderboard.api.BoardsManager;
import ro.nico.leaderboard.data.PlayerData;
import ro.nico.leaderboard.data.PlayerId;
import ro.nico.leaderboard.settings.MessageSettings;
import ro.nico.leaderboard.storage.SQLDateType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AstralLeaderboardsCommand implements TabExecutor {
    private final AstralLeaderboardsPlugin plugin;

    public AstralLeaderboardsCommand(AstralLeaderboardsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        MessageSettings messageSettings = plugin.getSettings().getMessageSettings();
        if (args.length == 0) {
            if (plugin.hasPermission(sender, "astralleaderboards.command.help")) this.sendHelp(sender);
            else
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageSettings.getNoPermissionMessage()));
        } else {
            switch (args[0].toLowerCase()) {
                case "help" -> {
                    if (plugin.hasPermission(sender, "astralleaderboards.command.help"))
                        this.sendHelp(sender);
                    else
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageSettings.getNoPermissionMessage()));
                }
                case "reload" -> {
                    if (plugin.hasPermission(sender, "astralleaderboards.command.reload")) {
                        plugin.reloadPlugin();
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageSettings.getReloadSuccessMessage()));
                    } else
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageSettings.getNoPermissionMessage()));
                }
                case "update" -> {
                    if (plugin.hasPermission(sender, "astralleaderboards.command.update")) {
                        if (args.length == 2 || args.length == 3) {
                            Board board = plugin.getBoardsManager().getBoard(args[1]);
                            if (board == null) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageSettings.getBoardNotFoundMessage().replace("%board%", args[1])));
                            } else {
                                boolean forceOffline = args.length == 3 && "--force-offline".equalsIgnoreCase(args[2]);
                                board.getBoardData().syncUpdate(forceOffline);
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageSettings.getBoardUpdatedMessage().replace("%board%", args[1])));
                            }
                        } else
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageSettings.getBoardUpdateUsageMessage()));
                    } else
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageSettings.getNoPermissionMessage()));
                }
                case "create" -> {
                    if (plugin.hasPermission(sender, "astralleaderboards.command.create")) {
                        if (args.length == 3) {
                            BoardsManager boardsManager = plugin.getBoardsManager();
                            if (boardsManager.hasBoard(args[1])) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageSettings.getBoardAlreadyExistsMessage().replace("%board%", args[1])));
                            } else {
                                if (!BoardsManager.BOARD_ID_PATTERN.matcher(args[1]).matches()) {
                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageSettings.getBoardInvalidIdMessage().replace("%board%", args[1])));
                                } else {
                                    try {
                                        Board board = plugin.getBoardsManager().createBoard(args[1], args[2]);
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageSettings.getBoardCreateSuccessMessage().replace("%board%", board.getId())));
                                    } catch (IOException | InvalidConfigurationException e) {
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageSettings.getBoardCreateFailMessage().replace("%board%", args[1])));
                                    }
                                }
                            }
                        } else
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageSettings.getBoardCreateUsageMessage()));
                    } else
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageSettings.getNoPermissionMessage()));
                }
                case "delete" -> {
                    if (plugin.hasPermission(sender, "astralleaderboards.command.delete")) {
                        if (args.length == 2) {
                            BoardsManager boardsManager = plugin.getBoardsManager();
                            if (!boardsManager.hasBoard(args[1])) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageSettings.getBoardNotFoundMessage().replace("%board%", args[1])));
                            } else {
                                boardsManager.deleteBoard(args[1]);
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageSettings.getBoardDeleteSuccessMessage().replace("%board%", args[1])));
                            }
                        } else
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageSettings.getBoardCreateUsageMessage()));
                    } else
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageSettings.getNoPermissionMessage()));
                }
                case "data" -> {
                    if (plugin.hasPermission(sender, "astralleaderboards.command.data")) {
                        if (args.length == 3 || args.length == 4) {
                            Board board = plugin.getBoardsManager().getBoard(args[1]);
                            if (board == null) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageSettings.getBoardNotFoundMessage().replace("%board%", args[1])));
                            } else {
                                try {
                                    SQLDateType dateType = SQLDateType.valueOf(args[2].toUpperCase());
                                    if (board.hasBoardUpdateType(dateType)) {
                                        int page = 1;
                                        if (args.length == 4) {
                                            try {
                                                page = Integer.parseInt(args[3]);
                                                if (page < 1)
                                                    page = 1;
                                            } catch (NumberFormatException ignore) {
                                            }
                                        }
                                        int start = ((page - 1) * 10) + 1;
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageSettings.getDataHeaderMessage()
                                                .replace("%board%", board.getId())
                                                .replace("%type%", dateType.name().toLowerCase())
                                                .replace("%page%", String.valueOf(page))));
                                        for (int i = start; i < start + 10; i++) {
                                            PlayerId key = board.getBoardData().getKey(i, dateType);
                                            PlayerData value = board.getBoardData().getData(i, dateType);
                                            sender.sendMessage(
                                                    ChatColor.translateAlternateColorCodes('&',
                                                            messageSettings.getDataHeaderEntryMessage()
                                                                    .replace("%name%", key.getName())
                                                                    .replace("%uuid%", key.getUuid().toString())
                                                                    .replace("%sorter%", value.getSorter())
                                                                    .replace("%rank%", String.valueOf(i))
                                                                    .replace("%trackers%", this.trackersToString(value.getTrackers()))));
                                        }
                                    } else
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageSettings.getBoardDateTypeNotEnabledMessage()
                                                .replace("%board%", board.getId())
                                                .replace("%type%", args[2])));
                                } catch (IllegalArgumentException e) {
                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageSettings.getInvalidDateTypeMessage()
                                            .replace("%board%", board.getId())
                                            .replace("%type%", args[2])
                                            .replace("%date_types%", Arrays.stream(SQLDateType.values()).map(type -> type.name().toLowerCase()).collect(Collectors.joining(", ")))));
                                }

                            }
                        } else
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageSettings.getBoardDataUsageMessage()));
                    } else
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageSettings.getNoPermissionMessage()));
                }
                case "addtracker" -> {
                    if (plugin.hasPermission(sender, "astralleaderboards.command.addtracker")) {
                        if (args.length == 4) {
                            Board board = plugin.getBoardsManager().getBoard(args[1]);
                            if (board == null) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageSettings.getBoardNotFoundMessage().replace("%board%", args[1])));
                            } else {
                                String trackerId = args[2];
                                String trackerValue = args[3];
                                if (board.getTrackers().containsKey(trackerId)) {
                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageSettings.getTrackerAlreadyExistsMessage().replace("%tracker%", trackerId)));
                                } else {
                                    board.addTracker(trackerId, trackerValue);
                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageSettings.getTrackerAddedMessage().replace("%tracker%", trackerId).replace("%value%", trackerValue)));
                                }
                            }
                        } else
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageSettings.getTrackerAddUsageMessage()));
                    } else
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageSettings.getNoPermissionMessage()));
                }
                case "removetracker" -> {
                    if (plugin.hasPermission(sender, "astralleaderboards.command.removetracker")) {
                        if (args.length == 3) {
                            Board board = plugin.getBoardsManager().getBoard(args[1]);
                            if (board == null) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageSettings.getBoardNotFoundMessage().replace("%board%", args[1])));
                            } else {
                                String trackerId = args[2];
                                if (!board.getTrackers().containsKey(trackerId)) {
                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageSettings.getTrackerNotFoundMessage().replace("%tracker%", trackerId)));
                                } else {
                                    board.removeTracker(trackerId);
                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageSettings.getTrackerRemovedMessage().replace("%tracker%", trackerId)));
                                }
                            }
                        } else
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageSettings.getTrackerRemoveUsageMessage()));
                    } else
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageSettings.getNoPermissionMessage()));
                }
            }
        }
        return true;
    }

    private String trackersToString(Map<String, String> trackers) {
        List<String> trackerList = new ArrayList<>();
        for (Map.Entry<String, String> entry : trackers.entrySet()) {
            trackerList.add(entry.getKey() + ": " + entry.getValue());
        }
        return String.join(", ", trackerList);
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], List.of("help", "reload", "update", "create", "delete", "data"), new ArrayList<>());
        } else if (args.length == 2) {
            if ("update".equalsIgnoreCase(args[0]) || "delete".equalsIgnoreCase(args[0]) || "data".equalsIgnoreCase(args[0])) {
                return StringUtil.copyPartialMatches(args[1], plugin.getBoardsManager().getBoardsIds(), new ArrayList<>());
            }
        } else if (args.length == 3) {
            if ("create".equalsIgnoreCase(args[0])) {
                if (args[2].startsWith("%")) {
                    return StringUtil.copyPartialMatches(args[2], PlaceholderAPI.getRegisteredIdentifiers().stream().map(str -> "%" + str).toList(), new ArrayList<>());
                }
            } else if ("data".equalsIgnoreCase(args[0])) {
                return StringUtil.copyPartialMatches(args[2], Arrays.stream(SQLDateType.values()).map(type -> type.name().toLowerCase()).toList(), new ArrayList<>());
            } else if ("update".equalsIgnoreCase(args[0])) {
                return StringUtil.copyPartialMatches(args[2], List.of("--force-offline"), new ArrayList<>());
            }
        }
        return List.of();
    }

    private void sendHelp(CommandSender sender) {
        for (String str : plugin.getSettings().getMessageSettings().getHelpMessages()) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', str));
        }
    }
}

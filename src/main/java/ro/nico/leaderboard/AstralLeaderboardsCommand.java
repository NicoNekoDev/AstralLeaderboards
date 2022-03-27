package ro.nico.leaderboard;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.nico.leaderboard.api.Board;

import java.util.List;

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
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no-permission", "&cYou don't have permission to use this command.")));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "help" -> {
                if (plugin.getVaultPermissions().has(sender, "astralleaderboards.command.help"))
                    this.sendHelp(sender);
                else
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no-permission", "&cYou don't have permission to use this command.")));
                return true;
            }
            case "reload" -> {
                if (!plugin.getVaultPermissions().has(sender, "astralleaderboards.command.reload")) {
                    plugin.reloadPlugin();
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.reload-success", "&aSuccessfully reloaded the plugin!")));
                } else
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no-permission", "&cYou don't have permission to use this command.")));
                return true;
            }
            case "update" -> {
                if (plugin.getVaultPermissions().has(sender, "astralleaderboards.command.update")) {
                    if (args.length == 2) {
                        Board board = plugin.getBoardsManager().getBoard(args[1]);
                        if (board == null) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.board-not-found", "&cThe board &e%board% &cwas not found!").replace("%board%", args[1])));
                            return true;
                        }
                        board.getBoardData().update();
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.board-updated", "&aSuccessfully updated the board &e%board%&a!").replace("%board%", args[1])));
                        return true;
                    }
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.update-usage", "&cUsage: &e/astrallb update <board>")));
                } else
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no-permission", "&cYou don't have permission to use this command.")));
                return true;
            }
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 0) {
            return List.of("help", "reload", "update");
        } else if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], List.of("help", "reload", "update"), List.of());
        } else if (args.length == 2) {
            if ("update".equalsIgnoreCase(args[0])) {
                return StringUtil.copyPartialMatches(args[1], plugin.getBoardsManager().getBoardsIds(), List.of());
            }
        }
        return List.of();
    }

    private void sendHelp(CommandSender sender) {
        for (String str : plugin.getConfig().isList("messages.help") ?
                plugin.getConfig().getStringList("messages.help") :
                List.of(
                        "&a&m====================",
                        "&a&l&oAstralLeaderboards",
                        "",
                        "&e/astrallb reload &7- &aReloads the config.",
                        "&e/astrallb update <board> &7- &aUpdates the board.",
                        "&e/astrallb help &7- &aShows this help.",
                        "",
                        "&a&m====================")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', str));
        }
    }
}

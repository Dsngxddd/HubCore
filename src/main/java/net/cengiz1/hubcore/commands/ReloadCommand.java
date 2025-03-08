package net.cengiz1.hubcore.commands;

import net.cengiz1.hubcore.HubCore;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {
    private final HubCore plugin;

    public ReloadCommand(HubCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("hubcore.reload")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.no-permission", "&cYou are not authorized to use this command!")));
            return true;
        }

        // Reload config
        plugin.reloadConfig();
        plugin.getConfigManager().reloadConfig();

        // Reload managers
        if (plugin.getAnnouncementManager() != null) {
            plugin.getAnnouncementManager().reload();
        }

        sender.sendMessage(ChatColor.GREEN + "HubCore successfully reinstalled!");
        return true;
    }
}
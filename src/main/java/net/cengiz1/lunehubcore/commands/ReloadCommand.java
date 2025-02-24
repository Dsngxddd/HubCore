package net.cengiz1.lunehubcore.commands;

import net.cengiz1.lunehubcore.LuneHubCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReloadCommand implements CommandExecutor {
    private final LuneHubCore plugin;

    public ReloadCommand(LuneHubCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cBu komut sadece oyuncular tarafından kullanılabilir!");
            return true;
        }
        if (!sender.hasPermission("multihub.reload")) {
            sender.sendMessage(plugin.getConfig().getString("messages.no-permission").replace("&", "§"));
            return true;
        }
        plugin.getConfigManager().reloadConfig();
        return true;
    }
}
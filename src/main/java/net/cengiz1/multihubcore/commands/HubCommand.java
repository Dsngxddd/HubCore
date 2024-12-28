package net.cengiz1.multihubcore.commands;

import net.cengiz1.multihubcore.MultiHubCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HubCommand implements CommandExecutor {
    private final MultiHubCore plugin;

    public HubCommand(MultiHubCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cBu komut sadece oyuncular tarafından kullanılabilir!");
            return true;
        }

        Player player = (Player) sender;
        String defaultLobby = plugin.getConfig().getString("settings.default-lobby");
        plugin.getLobbyManager().teleportToLobby(player, defaultLobby);

        return true;
    }
}
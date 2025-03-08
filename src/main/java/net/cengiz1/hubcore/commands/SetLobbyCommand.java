package net.cengiz1.hubcore.commands;

import net.cengiz1.hubcore.HubCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetLobbyCommand implements CommandExecutor {
    private final HubCore plugin;

    public SetLobbyCommand(HubCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cYou are not authorized to use this command!");
            return true;
        }

        if (!sender.hasPermission("multihub.setlobby")) {
            sender.sendMessage(plugin.getConfig().getString("messages.no-permission").replace("&", "§"));
            return true;
        }

        Player player = (Player) sender;
        String lobbyId = args.length > 0 ? args[0] : "default";

        plugin.getLobbyManager().setLobbySpawn(lobbyId, player.getLocation());

        String successMsg = plugin.getConfig().getString("messages.lobby-set")
                .replace("%lobby%", lobbyId)
                .replace("&", "§");
        player.sendMessage(successMsg);

        return true;
    }
}


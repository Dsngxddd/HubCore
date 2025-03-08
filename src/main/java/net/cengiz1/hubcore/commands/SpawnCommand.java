package net.cengiz1.hubcore.commands;

import net.cengiz1.hubcore.HubCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand implements CommandExecutor {
    private final HubCore plugin;

    public SpawnCommand(HubCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cYou are not authorized to use this command!");
            return true;
        }

        Player player = (Player) sender;
        String currentLobby = plugin.getLobbyManager().getPlayerLobby(player);
        plugin.getLobbyManager().teleportToLobby(player, currentLobby);

        return true;
    }
}
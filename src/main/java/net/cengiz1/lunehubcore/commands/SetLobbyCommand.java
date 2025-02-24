package net.cengiz1.lunehubcore.commands;

import net.cengiz1.lunehubcore.LuneHubCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetLobbyCommand implements CommandExecutor {
    private final LuneHubCore plugin;

    public SetLobbyCommand(LuneHubCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cBu komut sadece oyuncular tarafından kullanılabilir!");
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


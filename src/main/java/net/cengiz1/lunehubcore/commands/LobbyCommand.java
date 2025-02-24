package net.cengiz1.lunehubcore.commands;

import net.cengiz1.lunehubcore.LuneHubCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LobbyCommand implements CommandExecutor {
    private final LuneHubCore plugin;

    public LobbyCommand(LuneHubCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cBu komut sadece oyuncular tarafından kullanılabilir!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length > 0) {
            String lobbyId = args[0];
            if (!plugin.getConfig().contains("lobbies." + lobbyId)) {
                String invalidMsg = plugin.getConfig().getString("messages.invalid-lobby")
                        .replace("&", "§");
                player.sendMessage(invalidMsg);
                return true;
            }

            plugin.getLobbyManager().teleportToLobby(player, lobbyId);
        } else {
            plugin.getMenuManager().openLobbySelector(player);
        }

        return true;
    }
}
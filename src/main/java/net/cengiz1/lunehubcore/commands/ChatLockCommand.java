package net.cengiz1.lunehubcore.commands;

import net.cengiz1.lunehubcore.LuneHubCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChatLockCommand implements CommandExecutor {
    private final LuneHubCore plugin;

    public ChatLockCommand(LuneHubCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("lunehub.chatlock")) {
            String noPermMsg = plugin.getConfig().getString("messages.no-permission", "&cBu komutu kullanma yetkiniz yok!")
                    .replace("&", "§");
            sender.sendMessage(noPermMsg);
            return true;
        }
        boolean currentLockState = plugin.getConfig().getBoolean("chat.locked", false);
        boolean newLockState = !currentLockState;
        plugin.getConfig().set("chat.locked", newLockState);
        plugin.saveConfig();
        if (newLockState) {
            sender.sendMessage(ChatColor.GREEN + "Sohbet kilitlendi! Sadece OP yetkisi olanlar konuşabilir.");
        } else {
            sender.sendMessage(ChatColor.GREEN + "Sohbet kilidi açıldı! Herkes konuşabilir.");
        }
        String broadcastMessage;
        if (newLockState) {
            broadcastMessage = plugin.getConfig().getString("messages.chat-locked-broadcast", "&c&lSohbet kilitlendi! Sadece yetkililer konuşabilir.");
        } else {
            broadcastMessage = plugin.getConfig().getString("messages.chat-unlocked-broadcast", "&a&lSohbet kilidi açıldı! Herkes konuşabilir.");
        }
        String announcer = "Konsol";
        if (sender instanceof Player) {
            announcer = ((Player) sender).getName();
        }
        String announcement = plugin.getConfig().getString("messages.chat-lock-format", "&8[&e!&8] %message% &7(%announcer%)")
                .replace("%message%", broadcastMessage)
                .replace("%announcer%", announcer)
                .replace("&", "§");
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(announcement);
        }

        return true;
    }
}
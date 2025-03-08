package net.cengiz1.hubcore.commands;

import net.cengiz1.hubcore.HubCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChatLockCommand
implements CommandExecutor {
    private final HubCore plugin;

    public ChatLockCommand(HubCore plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("hubcore.chatlock")) {
            String noPermMsg = this.plugin.getConfig().getString("messages.no-permission", "&cBu komutu kullanma yetkiniz yok!").replace("&", "§");
            sender.sendMessage(noPermMsg);
            return true;
        }
        boolean currentLockState = this.plugin.getConfig().getBoolean("chat.locked", false);
        boolean newLockState = !currentLockState;
        this.plugin.getConfig().set("chat.locked", (Object)newLockState);
        this.plugin.saveConfig();
        if (newLockState) {
            sender.sendMessage(ChatColor.GREEN + "Chat locked! Only those authorized to OP can talk");
        } else {
            sender.sendMessage(ChatColor.GREEN + "Sohbet kilidi açıldı Herkes konuşabilir.");
        }
        String broadcastMessage = newLockState ? this.plugin.getConfig().getString("messages.chat-locked-broadcast", "&c&lSohbet kilitlendi! Sadece yetkililer konuşabilir.") : this.plugin.getConfig().getString("messages.chat-unlocked-broadcast", "&a&lSohbet kilidi a\u00e7\u0131ld\u0131! Herkes konu\u015fabilir.");
        String announcer = "Konsol";
        if (sender instanceof Player) {
            announcer = ((Player)sender).getName();
        }
        String announcement = this.plugin.getConfig().getString("messages.chat-lock-format", "&8[&e!&8] %message% &7(%announcer%)").replace("%message%", broadcastMessage).replace("%announcer%", announcer).replace("&", "§");
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(announcement);
        }
        return true;
    }
}


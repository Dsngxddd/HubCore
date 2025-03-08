package net.cengiz1.hubcore.listener;

import net.cengiz1.hubcore.HubCore;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener
implements Listener {
    private final HubCore plugin;

    public ChatListener(HubCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        boolean chatLocked = this.plugin.getConfig().getBoolean("chat.locked", false);
        if (chatLocked && !player.isOp() && !player.hasPermission("hubcore.chatbypass")) {
            event.setCancelled(true);
            String message = this.plugin.getConfig().getString("messages.chat-locked", "&cChat is currently locked! Only authorized persons can talk.");
            player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)message));
        }
    }
}


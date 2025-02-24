package net.cengiz1.lunehubcore.listener;

import net.cengiz1.lunehubcore.LuneHubCore;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {
    private final LuneHubCore plugin;

    public ChatListener(LuneHubCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        boolean chatLocked = plugin.getConfig().getBoolean("chat.locked", false);
        if (chatLocked && !player.isOp() && !player.hasPermission("lunehub.chatbypass")) {
            event.setCancelled(true);
            String message = plugin.getConfig().getString("messages.chat-locked", "&cSohbet şu anda kilitli! Sadece yetkililer konuşabilir.");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }
}
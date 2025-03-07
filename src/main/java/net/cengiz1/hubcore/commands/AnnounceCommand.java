package net.cengiz1.hubcore.commands;

import net.cengiz1.hubcore.HubCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class AnnounceCommand implements CommandExecutor {
    private final HubCore plugin;

    public AnnounceCommand(HubCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("hubcore.announce")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.no-permission", "&cBu komutu kullanma yetkiniz yok!")));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Kullanım: /announce <mesaj> veya /announce next");
            return true;
        }

        // Check if the command is to send the next scheduled announcement
        if (args.length == 1 && args[0].equalsIgnoreCase("next")) {
            plugin.getAnnouncementManager().sendNextAnnouncement();
            sender.sendMessage(ChatColor.GREEN + "Sıradaki otomatik duyuru gönderildi.");
            return true;
        }

        // Otherwise, build the custom announcement message
        StringBuilder message = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            message.append(args[i]).append(" ");
        }

        // Format and send the announcement
        String prefix = plugin.getConfig().getString("announcements.prefix", "&8[&e&lDUYURU&8]");
        prefix = ChatColor.translateAlternateColorCodes('&', prefix);

        String finalMessage = ChatColor.translateAlternateColorCodes('&', message.toString().trim());

        // Check if formats are specified
        List<String> formats = plugin.getConfig().getStringList("announcements.formats");
        if (formats.isEmpty()) {
            // Default format - just send the message with prefix
            Bukkit.broadcastMessage(prefix + " " + finalMessage);
        } else {
            // Send each format line
            for (String line : formats) {
                line = line.replace("%prefix%", prefix)
                        .replace("%message%", finalMessage);
                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', line));
            }
        }

        // Play sound if enabled
        if (plugin.getConfig().getBoolean("announcements.sound.enabled", false)) {
            String soundName = plugin.getConfig().getString("announcements.sound.name", "ENTITY_EXPERIENCE_ORB_PICKUP");
            float volume = (float) plugin.getConfig().getDouble("announcements.sound.volume", 1.0);
            float pitch = (float) plugin.getConfig().getDouble("announcements.sound.pitch", 1.0);

            try {
                Sound sound = Sound.valueOf(soundName);
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.playSound(player.getLocation(), sound, volume, pitch);
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid sound name in config: " + soundName);
            }
        }

        sender.sendMessage(ChatColor.GREEN + "Duyuru gönderildi: " + ChatColor.RESET + finalMessage);
        return true;
    }
}
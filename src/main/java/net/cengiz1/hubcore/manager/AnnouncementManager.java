package net.cengiz1.hubcore.manager;

import net.cengiz1.hubcore.HubCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Random;

public class AnnouncementManager {
    private final HubCore plugin;
    private final Random random;
    private BukkitTask announcementTask;
    private int currentIndex = 0;

    public AnnouncementManager(HubCore plugin) {
        this.plugin = plugin;
        this.random = new Random();

        startAnnouncementTask();
    }

    /**
     * Starts the auto announcement task if enabled in config
     */
    public void startAnnouncementTask() {
        // Cancel existing task if running
        stopAnnouncementTask();

        // Check if announcements are enabled
        if (!plugin.getConfig().getBoolean("announcements.enabled", false)) {
            return;
        }

        // Get the interval in seconds
        int interval = plugin.getConfig().getInt("announcements.interval", 300);
        if (interval < 10) interval = 10; // Minimum of 10 seconds

        // Schedule the task
        announcementTask = Bukkit.getScheduler().runTaskTimer(plugin, this::broadcastAnnouncement,
                20L * plugin.getConfig().getInt("announcements.initial-delay", 60),
                20L * interval);

        plugin.getLogger().info("Auto announcements started with interval of " + interval + " seconds");
    }

    /**
     * Stops the announcement task if running
     */
    public void stopAnnouncementTask() {
        if (announcementTask != null) {
            announcementTask.cancel();
            announcementTask = null;
        }
    }

    /**
     * Sends a broadcast announcement to all players
     */
    private void broadcastAnnouncement() {
        List<String> announcements = plugin.getConfig().getStringList("announcements.messages");
        if (announcements.isEmpty()) {
            return;
        }

        // Get the next announcement
        String message;
        boolean random = plugin.getConfig().getBoolean("announcements.random", false);

        if (random) {
            // Get a random announcement
            message = announcements.get(this.random.nextInt(announcements.size()));
        } else {
            // Get next announcement in sequence
            message = announcements.get(currentIndex);
            currentIndex = (currentIndex + 1) % announcements.size();
        }

        // Format prefix and message
        String prefix = plugin.getConfig().getString("announcements.prefix", "&8[&e&lDUYURU&8]");
        prefix = ChatColor.translateAlternateColorCodes('&', prefix);

        message = ChatColor.translateAlternateColorCodes('&', message);

        // Broadcast formats
        List<String> formats = plugin.getConfig().getStringList("announcements.formats");
        if (formats.isEmpty()) {
            // Default format - just send the message with prefix
            Bukkit.broadcastMessage(prefix + " " + message);
        } else {
            // Send each format line
            for (String line : formats) {
                line = line.replace("%prefix%", prefix)
                        .replace("%message%", message);
                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', line));
            }
        }

        // Play sound if enabled
        if (plugin.getConfig().getBoolean("announcements.sound.enabled", false)) {
            String soundName = plugin.getConfig().getString("announcements.sound.name", "entity.player.levelup");
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
    }

    /**
     * Manually sends the next announcement
     */
    public void sendNextAnnouncement() {
        broadcastAnnouncement();
    }

    /**
     * Reloads the announcement configuration
     */
    public void reload() {
        stopAnnouncementTask();
        startAnnouncementTask();
    }
}
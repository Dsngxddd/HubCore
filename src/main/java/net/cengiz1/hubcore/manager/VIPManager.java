package net.cengiz1.hubcore.manager;

import net.cengiz1.hubcore.HubCore;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Random;

public class VIPManager {
    private final HubCore plugin;
    private final Random random;

    public VIPManager(HubCore plugin) {
        this.plugin = plugin;
        this.random = new Random();
    }

    /**
     * Handles the join message for a player based on their permissions
     * @param player The player who joined
     * @return true if a VIP message was sent, false otherwise
     */
    public boolean handleVIPJoin(Player player) {
        ConfigurationSection vipSection = plugin.getConfig().getConfigurationSection("vip-messages");
        if (vipSection == null) return false;

        for (String vipGroup : vipSection.getKeys(false)) {
            String permission = vipSection.getString(vipGroup + ".permission");
            if (permission != null && player.hasPermission(permission)) {
                sendVIPJoinMessage(player, vipGroup);
                return true;
            }
        }
        return false;
    }

    /**
     * Sends a VIP join message for the given player and VIP group
     * @param player The player who joined
     * @param vipGroup The VIP group configuration key
     */
    private void sendVIPJoinMessage(Player player, String vipGroup) {
        ConfigurationSection vipSection = plugin.getConfig().getConfigurationSection("vip-messages." + vipGroup);
        if (vipSection == null) return;

        // Get the message format
        String format = vipSection.getString("format", "&e[&6VIP&e] &b%player% &7has joined the server!");

        // Check if we should use random messages
        if (vipSection.contains("messages") && vipSection.isList("messages")) {
            List<String> messages = vipSection.getStringList("messages");
            if (!messages.isEmpty()) {
                // Select a random message
                String randomMessage = messages.get(random.nextInt(messages.size()));
                format = randomMessage;
            }
        }

        // Process the message
        String finalMessage = format
                .replace("%player%", player.getName())
                .replace("%displayname%", player.getDisplayName())
                .replace("&", "§");

        // Send to all players or just to the player
        boolean broadcast = vipSection.getBoolean("broadcast", true);
        if (broadcast) {
            Bukkit.broadcastMessage(finalMessage);
        } else {
            player.sendMessage(finalMessage);
        }

        // Play sound if enabled
        if (vipSection.getBoolean("play-sound", false)) {
            String sound = vipSection.getString("sound", "ENTITY_EXPERIENCE_ORB_PICKUP");
            float volume = (float) vipSection.getDouble("sound-volume", 1.0);
            float pitch = (float) vipSection.getDouble("sound-pitch", 1.0);

            if (broadcast) {
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    onlinePlayer.playSound(onlinePlayer.getLocation(), sound, volume, pitch);
                }
            } else {
                player.playSound(player.getLocation(), sound, volume, pitch);
            }
        }
    }
}
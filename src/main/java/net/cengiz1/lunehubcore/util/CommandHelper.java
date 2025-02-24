package net.cengiz1.lunehubcore.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.logging.Level;

/**
 * Komut çalıştırma işlemlerini kolaylaştıran yardımcı sınıf
 */
public class CommandHelper {

    private final JavaPlugin plugin;

    public CommandHelper(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * @param player
     * @param command
     */
    public void runCommand(Player player, String command) {
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        if (command.trim().isEmpty()) {
            plugin.getLogger().log(Level.WARNING, "Boş komut çalıştırılamaz!");
            return;
        }

        final String finalCommand = command;
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    boolean success = player.performCommand(finalCommand);

                    if (!success) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
                    }
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Komut çalıştırma hatası: " + e.getMessage(), e);
                }
            }
        }.runTask(plugin);
    }

}
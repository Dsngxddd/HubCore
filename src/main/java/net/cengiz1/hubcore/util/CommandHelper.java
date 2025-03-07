package net.cengiz1.hubcore.util;

import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class CommandHelper {
    private final JavaPlugin plugin;

    public CommandHelper(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void runCommand(final Player player, String command) {
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        if (command.trim().isEmpty()) {
            this.plugin.getLogger().log(Level.WARNING, "Bo\u015f komut \u00e7al\u0131\u015ft\u0131r\u0131lamaz!");
            return;
        }
        final String finalCommand = command;
        new BukkitRunnable(){

            public void run() {
                try {
                    boolean success = player.performCommand(finalCommand);
                    if (!success) {
                        Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), (String)finalCommand);
                    }
                } catch (Exception e) {
                    CommandHelper.this.plugin.getLogger().log(Level.SEVERE, "Komut \u00e7al\u0131\u015ft\u0131rma hatas\u0131: " + e.getMessage(), e);
                }
            }
        }.runTask((Plugin)this.plugin);
    }
}


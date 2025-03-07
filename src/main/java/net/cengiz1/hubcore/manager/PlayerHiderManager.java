package net.cengiz1.hubcore.manager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PlayerHiderManager {

    private final Set<Player> hiddenPlayers;
    private final Map<Player, Long> playerHideTimestamps;
    private final JavaPlugin plugin;

    public PlayerHiderManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.hiddenPlayers = new HashSet<>();
        this.playerHideTimestamps = new HashMap<>();
    }

    public void hidePlayers(Player player) {
        if (hiddenPlayers.contains(player)) return;

        for (Player target : Bukkit.getOnlinePlayers()) {
            if (!player.equals(target)) {
                player.hidePlayer(plugin, target);
            }
        }
        hiddenPlayers.add(player);
        playerHideTimestamps.put(player, System.currentTimeMillis());
    }

    public void showPlayers(Player player) {
        if (!hiddenPlayers.contains(player)) return;

        for (Player target : Bukkit.getOnlinePlayers()) {
            if (!player.equals(target)) {
                player.showPlayer(plugin, target);
            }
        }
        hiddenPlayers.remove(player);
    }

    public boolean isHidingPlayers(Player player) {
        return hiddenPlayers.contains(player);
    }

    public void clearPlayer(Player player) {
        hiddenPlayers.remove(player);
        playerHideTimestamps.remove(player);
    }

    public void setPlayerHidingStatus(Player player, boolean hide) {
        if (hide) {
            hidePlayers(player);
        } else {
            showPlayers(player);
        }
    }

    public void showAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (Player target : Bukkit.getOnlinePlayers()) {
                if (!player.equals(target)) {
                    // Düzeltilmiş API çağrısı
                    player.showPlayer(plugin, target);
                }
            }
        }
        hiddenPlayers.clear();
    }

    public void hideAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (Player target : Bukkit.getOnlinePlayers()) {
                if (!player.equals(target)) {
                    // Düzeltilmiş API çağrısı
                    player.hidePlayer(plugin, target);
                }
            }
        }
        hiddenPlayers.addAll(Bukkit.getOnlinePlayers());
    }
}
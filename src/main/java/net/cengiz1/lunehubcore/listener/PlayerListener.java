package net.cengiz1.lunehubcore.listener;

import net.cengiz1.lunehubcore.LuneHubCore;
import net.cengiz1.lunehubcore.manager.DatabaseManager;
import net.cengiz1.lunehubcore.manager.ItemManager;
import net.cengiz1.lunehubcore.manager.PlayerHiderManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PlayerListener implements Listener {
    private final LuneHubCore plugin;

    public PlayerListener(LuneHubCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (plugin.getConfig().getBoolean("messages.join.enabled")) {
            String joinMessage = plugin.getConfig().getString("messages.join.format")
                    .replace("%player%", event.getPlayer().getName())
                    .replace("&", "§");
            event.setJoinMessage(joinMessage);
        }
        String defaultLobby = plugin.getConfig().getString("settings.default-lobby");
        plugin.getLobbyManager().teleportToLobby(event.getPlayer(), defaultLobby);
        if (plugin.getConfig().getBoolean("mysql.enabled")) {
            updatePlayerStats(event.getPlayer());
        }
        plugin.getItemManager().giveLobbyItems(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (plugin.getConfig().getBoolean("messages.quit.enabled")) {
            String quitMessage = plugin.getConfig().getString("messages.quit.format")
                    .replace("%player%", event.getPlayer().getName())
                    .replace("&", "§");
            event.setQuitMessage(quitMessage);
        }
        plugin.getParticleManager().stopPlayerEffects(event.getPlayer());
        plugin.getLobbyManager().removePlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;

        String itemName = item.getItemMeta().getDisplayName().replace("&", "§");
        PlayerHiderManager playerHiderManager = plugin.getPlayerHiderManager();

        String serverSelectorName = plugin.getConfig().getString("items.server-selector.display-name");
        if (serverSelectorName != null && itemName.equals(serverSelectorName.replace("&", "§"))) {
            event.setCancelled(true);
            plugin.getMenuManager().openServerSelector(player);
            return;
        }

        String lobbySelectorName = plugin.getConfig().getString("items.lobby-selector.display-name");
        if (lobbySelectorName != null && itemName.equals(lobbySelectorName.replace("&", "§"))) {
            event.setCancelled(true);
            plugin.getMenuManager().openLobbySelector(player);
            return;
        }

        String playerHiderName = plugin.getConfig().getString("items.player-hider.display-name");
        if (playerHiderName != null && itemName.equals(playerHiderName.replace("&", "§"))) {
            event.setCancelled(true);
            if (playerHiderManager.isHidingPlayers(player)) {
                playerHiderManager.showPlayers(player);
            } else {
                playerHiderManager.hidePlayers(player);
            }
        }
    }

    private void updatePlayerStats(Player player) {
        try {
            Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO " + plugin.getConfig().getString("mysql.table-prefix") + "stats " +
                            "(uuid, username, last_lobby, join_count) VALUES (?, ?, ?, 1) " +
                            "ON DUPLICATE KEY UPDATE username = ?, last_lobby = ?, join_count = join_count + 1, last_join = CURRENT_TIMESTAMP"
            );

            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, player.getName());
            ps.setString(3, plugin.getLobbyManager().getPlayerLobby(player));
            ps.setString(4, player.getName());
            ps.setString(5, plugin.getLobbyManager().getPlayerLobby(player));

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
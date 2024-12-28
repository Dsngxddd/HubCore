package net.cengiz1.multihubcore.listener;

import net.cengiz1.multihubcore.MultiHubCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.meta.ItemMeta;

public class InventoryListener implements Listener {
    private final MultiHubCore plugin;
    private final String serverSelectorTitle;
    private final String lobbySelectorTitle;

    public InventoryListener(MultiHubCore plugin) {
        this.plugin = plugin;
        this.serverSelectorTitle = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("menus.server-selector.title", "Server Selector"));
        this.lobbySelectorTitle = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("menus.lobby-selector.title", "Lobby Selector"));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        String title;

        try {
            title = event.getView().getTitle();
        } catch (Throwable e) {
            return;
        }

        if (title == null) {
            return;
        }

        if (!title.equals(serverSelectorTitle) && !title.equals(lobbySelectorTitle)) {
            return;
        }

        event.setCancelled(true);

        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) {
            return;
        }

        ItemMeta itemMeta = event.getCurrentItem().getItemMeta();
        if (!itemMeta.hasDisplayName()) {
            return;
        }

        String itemName = itemMeta.getDisplayName();

        if (plugin.getConfig().contains("servers")) {
            for (String serverId : plugin.getConfig().getConfigurationSection("servers").getKeys(false)) {
                String serverName = ChatColor.translateAlternateColorCodes('&',
                        plugin.getConfig().getString("servers." + serverId + ".display-name"));
                if (serverName != null && itemName.equals(serverName)) {
                    plugin.getServerManager().connectToServer(player, serverId);
                    return;
                }
            }
        }

        if (plugin.getConfig().contains("lobbies")) {
            for (String lobbyId : plugin.getConfig().getConfigurationSection("lobbies").getKeys(false)) {
                String lobbyName = ChatColor.translateAlternateColorCodes('&',
                        plugin.getConfig().getString("lobbies." + lobbyId + ".display-name"));
                if (lobbyName != null && itemName.equals(lobbyName)) {
                    plugin.getLobbyManager().teleportToLobby(player, lobbyId);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        String title;

        try {
            title = event.getView().getTitle();
        } catch (Throwable e) {
            return;
        }

        if (title == null) {
            return;
        }

        if (title.equals(serverSelectorTitle) || title.equals(lobbySelectorTitle)) {
            event.setCancelled(true);
        }
    }
}

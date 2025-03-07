package net.cengiz1.hubcore.listener;

import net.cengiz1.hubcore.HubCore;
import net.cengiz1.hubcore.util.CommandHelper;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.meta.ItemMeta;

public class InventoryListener implements Listener {
    private final HubCore plugin;
    private final String serverSelectorTitle;
    private final String lobbySelectorTitle;
    private final CommandHelper commandHelper;

    public InventoryListener(HubCore plugin) {
        this.plugin = plugin;
        this.serverSelectorTitle = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("menus.server-selector.title", "Server Selector"));
        this.lobbySelectorTitle = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("menus.lobby-selector.title", "Lobby Selector"));
        this.commandHelper = new CommandHelper(plugin);

        // Başlangıçta menü başlıklarını logla (debug için)
        plugin.getLogger().info("Server Selector Title: " + serverSelectorTitle);
        plugin.getLogger().info("Lobby Selector Title: " + lobbySelectorTitle);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        // Her tıklamayı logla (debug için)
        plugin.getLogger().info("Inventory click detected: " + title);

        // Tüm inventory click olaylarında her zaman item alanını kilitliyoruz
        if (event.getClickedInventory() != null && event.getClickedInventory().equals(player.getInventory())) {
            plugin.getLogger().info("Player clicked their own inventory");
            event.setCancelled(true);
            return;
        }

        // Menü kontrolü - tam başlık eşleşmesi
        if (title.equals(serverSelectorTitle) || title.equals(lobbySelectorTitle)) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null) {
                plugin.getLogger().info("Clicked item is null");
                return;
            }

            if (!event.getCurrentItem().hasItemMeta()) {
                plugin.getLogger().info("Clicked item has no meta");
                return;
            }

            ItemMeta itemMeta = event.getCurrentItem().getItemMeta();
            if (!itemMeta.hasDisplayName()) {
                plugin.getLogger().info("Clicked item has no display name");
                return;
            }

            String itemName = itemMeta.getDisplayName();
            plugin.getLogger().info("Item clicked: " + itemName);

            if (title.equals(serverSelectorTitle)) {
                handleServerSelection(player, itemName);
            } else if (title.equals(lobbySelectorTitle)) {
                handleLobbySelection(player, itemName);
            }
        }
    }

    private void handleServerSelection(Player player, String itemName) {
        if (!plugin.getConfig().contains("servers")) {
            plugin.getLogger().warning("No servers section found in config");
            return;
        }

        plugin.getLogger().info("Processing server selection: " + itemName);

        for (String serverId : plugin.getConfig().getConfigurationSection("servers").getKeys(false)) {
            String serverDisplayName = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("servers." + serverId + ".display-name", ""));

            plugin.getLogger().info("Checking server: " + serverId + " with display name: " + serverDisplayName);

            if (itemName.equals(serverDisplayName)) {
                String serverName = plugin.getConfig().getString("servers." + serverId + ".server-name", "");
                plugin.getLogger().info("Sending player to server: " + serverName);

                player.closeInventory();
                plugin.getServerManager().connectToServer(player, serverId);
                return;
            }
        }

        plugin.getLogger().warning("No matching server found for name: " + itemName);
    }

    private void handleLobbySelection(Player player, String itemName) {
        if (!plugin.getConfig().contains("lobbies")) {
            plugin.getLogger().warning("No lobbies section found in config");
            return;
        }

        plugin.getLogger().info("Processing lobby selection: " + itemName);

        for (String lobbyId : plugin.getConfig().getConfigurationSection("lobbies").getKeys(false)) {
            String lobbyName = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("lobbies." + lobbyId + ".display-name", ""));

            plugin.getLogger().info("Checking lobby: " + lobbyId + " with display name: " + lobbyName);

            if (itemName.equals(lobbyName)) {
                plugin.getLogger().info("Teleporting to lobby: " + lobbyId);
                player.closeInventory();
                plugin.getLobbyManager().teleportToLobby(player, lobbyId);
                return;
            }
        }

        plugin.getLogger().warning("No matching lobby found for name: " + itemName);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        String title = event.getView().getTitle();

        if (title.equals(serverSelectorTitle) || title.equals(lobbySelectorTitle)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        String title = event.getView().getTitle();

        if ((title.equals(serverSelectorTitle) || title.equals(lobbySelectorTitle)) && plugin.getMenuManager() != null) {
            plugin.getMenuManager().removeOpenMenu(player);
        }
    }
}
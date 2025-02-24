package net.cengiz1.lunehubcore.listener;

import net.cengiz1.lunehubcore.LuneHubCore;
import net.cengiz1.lunehubcore.util.CommandHelper;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.logging.Level;

public class InventoryListener implements Listener {
    private final LuneHubCore plugin;
    private final String serverSelectorTitle;
    private final String lobbySelectorTitle;
    private final CommandHelper commandHelper;

    public InventoryListener(LuneHubCore plugin) {
        this.plugin = plugin;
        this.serverSelectorTitle = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("menus.server-selector.title", "Server Selector"));
        this.lobbySelectorTitle = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("menus.lobby-selector.title", "Lobby Selector"));
        this.commandHelper = new CommandHelper(plugin);
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
        if (title.equals(serverSelectorTitle)) {
            handleServerSelection(player, itemName);
            return;
        }
        if (title.equals(lobbySelectorTitle)) {
            handleLobbySelection(player, itemName);
            return;
        }
    }

    private void handleServerSelection(Player player, String itemName) {
        if (!plugin.getConfig().contains("servers")) {
            return;
        }

        for (String serverId : plugin.getConfig().getConfigurationSection("servers").getKeys(false)) {
            String serverDisplayName = ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("servers." + serverId + ".display-name", ""));
            if (itemName.equals(serverDisplayName)) {
                plugin.getLogger().log(Level.INFO, "Sunucu bulundu: " + serverId);
                String command = plugin.getConfig().getString("servers." + serverId + ".command", "");
                String serverName = plugin.getConfig().getString("servers." + serverId + ".server-name", "");
                if (command.isEmpty()) {
                    command = "queue " + serverName;
                } else {
                    command = command.replace("%server%", serverName);
                }
                player.closeInventory();
                commandHelper.runCommand(player, command);
                return;
            }
        }
    }

    private void handleLobbySelection(Player player, String itemName) {
        if (!plugin.getConfig().contains("lobbies")) {
            return;
        }

        for (String lobbyId : plugin.getConfig().getConfigurationSection("lobbies").getKeys(false)) {
            String lobbyName = ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("lobbies." + lobbyId + ".display-name", ""));

            if (itemName.equals(lobbyName)) {
                plugin.getLobbyManager().teleportToLobby(player, lobbyId);
                return;
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

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        String title = event.getView().getTitle();

        if (title.equals(serverSelectorTitle) || title.equals(lobbySelectorTitle)) {
            if (plugin.getMenuManager() != null) {
                plugin.getMenuManager().removeOpenMenu(player);
            }
        }
    }
}
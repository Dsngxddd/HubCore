package net.cengiz1.multihubcore.manager;

import net.cengiz1.multihubcore.MultiHubCore;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.List;
import java.util.stream.Collectors;

public class MenuManager {
    private final MultiHubCore plugin;

    public MenuManager(MultiHubCore plugin) {
        this.plugin = plugin;
    }

    public void openServerSelector(Player player) {
        ConfigurationSection menuConfig = plugin.getConfig().getConfigurationSection("menus.server-selector");
        String title = menuConfig.getString("title").replace("&", "§");
        int size = menuConfig.getInt("size");

        Inventory inventory = Bukkit.createInventory(null, size, title);
        if (menuConfig.getBoolean("fill-empty")) {
            ItemStack filler = new ItemStack(Material.valueOf(menuConfig.getString("empty-material")));
            for (int i = 0; i < size; i++) {
                inventory.setItem(i, filler);
            }
        }
        ConfigurationSection servers = plugin.getConfig().getConfigurationSection("servers");
        for (String serverId : servers.getKeys(false)) {
            ConfigurationSection server = servers.getConfigurationSection(serverId);

            ItemStack icon = new ItemStack(Material.valueOf(server.getString("icon.material")));
            ItemMeta meta = icon.getItemMeta();

            meta.setDisplayName(server.getString("display-name").replace("&", "§"));
            List<String> lore = server.getStringList("description").stream()
                    .map(line -> line.replace("&", "§"))
                    .map(line -> line.replace("%online%",
                            plugin.getServerManager().getServerInfo(serverId).isOnline() ? "§aAKTİF" : "§cKAPALI"))
                    .collect(Collectors.toList());
            meta.setLore(lore);

            icon.setItemMeta(meta);
            inventory.setItem(server.getInt("slot"), icon);
        }

        player.openInventory(inventory);
    }

    public void openLobbySelector(Player player) {
        ConfigurationSection menuConfig = plugin.getConfig().getConfigurationSection("menus.lobby-selector");
        String title = menuConfig.getString("title").replace("&", "§");
        int size = menuConfig.getInt("size");

        Inventory inventory = Bukkit.createInventory(null, size, title);
        if (menuConfig.getBoolean("fill-empty")) {
            ItemStack filler = new ItemStack(Material.valueOf(menuConfig.getString("empty-material")));
            for (int i = 0; i < size; i++) {
                inventory.setItem(i, filler);
            }
        }

        ConfigurationSection lobbies = plugin.getConfig().getConfigurationSection("lobbies");
        for (String lobbyId : lobbies.getKeys(false)) {
            ConfigurationSection lobby = lobbies.getConfigurationSection(lobbyId);

            ItemStack icon = new ItemStack(Material.valueOf(lobby.getString("icon.material")));
            ItemMeta meta = icon.getItemMeta();

            meta.setDisplayName(lobby.getString("display-name").replace("&", "§"));
            List<String> lore = lobby.getStringList("description").stream()
                    .map(line -> line.replace("&", "§"))
                    .collect(Collectors.toList());
            int currentPlayers = plugin.getLobbyManager().getLobbyPlayerCount(lobbyId);
            int maxPlayers = lobby.getInt("max-players");
            lore.add("§7Oyuncular: §a" + currentPlayers + "§7/§c" + maxPlayers);

            meta.setLore(lore);
            icon.setItemMeta(meta);

            inventory.setItem(lobby.getInt("slot", 0), icon);
        }

        player.openInventory(inventory);
    }
}
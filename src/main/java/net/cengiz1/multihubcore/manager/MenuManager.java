package net.cengiz1.multihubcore.manager;

import net.cengiz1.multihubcore.MultiHubCore;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MenuManager {
    private final MultiHubCore plugin;
    private static final Material DEFAULT_MATERIAL = Material.STONE;
    private static final String DEFAULT_TITLE = "Menu";
    private static final int DEFAULT_SIZE = 27;

    public MenuManager(MultiHubCore plugin) {
        this.plugin = plugin;
    }

    public void openServerSelector(Player player) {
        if (plugin.getConfig() == null) {
            player.sendMessage("§cKonfigürasyon yüklenemedi!");
            return;
        }

        ConfigurationSection menuConfig = plugin.getConfig().getConfigurationSection("menus.server-selector");
        if (menuConfig == null) {
            player.sendMessage("§cServer selector menüsü bulunamadı!");
            return;
        }

        String title = menuConfig.getString("title", DEFAULT_TITLE).replace("&", "§");
        int size = menuConfig.getInt("size", DEFAULT_SIZE);

        Inventory inventory = Bukkit.createInventory(null, size, title);

        // Boş slotları doldur
        if (menuConfig.getBoolean("fill-empty", false)) {
            String emptyMaterial = menuConfig.getString("empty-material", "GRAY_STAINED_GLASS_PANE");
            Material fillerMaterial;
            try {
                fillerMaterial = Material.valueOf(emptyMaterial);
            } catch (IllegalArgumentException e) {
                fillerMaterial = Material.GRAY_STAINED_GLASS_PANE;
            }
            ItemStack filler = new ItemStack(fillerMaterial);
            for (int i = 0; i < size; i++) {
                inventory.setItem(i, filler);
            }
        }

        // Sunucuları ekle
        ConfigurationSection servers = plugin.getConfig().getConfigurationSection("servers");
        if (servers != null) {
            for (String serverId : servers.getKeys(false)) {
                ConfigurationSection server = servers.getConfigurationSection(serverId);
                if (server == null) continue;

                try {
                    String materialName = server.getString("icon.material", "GRASS_BLOCK");
                    Material material = Material.valueOf(materialName);
                    ItemStack icon = new ItemStack(material);
                    ItemMeta meta = icon.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(server.getString("display-name", serverId).replace("&", "§"));
                        icon.setItemMeta(meta);
                    }
                    int slot = server.getInt("slot", 0);
                    if (slot >= 0 && slot < size) {
                        inventory.setItem(slot, icon);
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Geçersiz materyal: " + serverId + " için!");
                }
            }
        }

        player.openInventory(inventory);
    }

    public void openLobbySelector(Player player) {
        if (plugin.getConfig() == null) {
            player.sendMessage("§cKonfigürasyon yüklenemedi!");
            return;
        }

        ConfigurationSection menuConfig = plugin.getConfig().getConfigurationSection("menus.lobby-selector");
        if (menuConfig == null) {
            player.sendMessage("§cLobby selector menüsü bulunamadı!");
            return;
        }

        String title = menuConfig.getString("title", DEFAULT_TITLE).replace("&", "§");
        int size = menuConfig.getInt("size", DEFAULT_SIZE);

        Inventory inventory = Bukkit.createInventory(null, size, title);

        // Boş slotları doldur
        if (menuConfig.getBoolean("fill-empty", false)) {
            String emptyMaterial = menuConfig.getString("empty-material", "GRAY_STAINED_GLASS_PANE");
            Material fillerMaterial;
            try {
                fillerMaterial = Material.valueOf(emptyMaterial);
            } catch (IllegalArgumentException e) {
                fillerMaterial = Material.GRAY_STAINED_GLASS_PANE;
            }
            ItemStack filler = new ItemStack(fillerMaterial);
            for (int i = 0; i < size; i++) {
                inventory.setItem(i, filler);
            }
        }

        // Lobileri ekle
        ConfigurationSection lobbies = plugin.getConfig().getConfigurationSection("lobbies");
        if (lobbies != null) {
            for (String lobbyId : lobbies.getKeys(false)) {
                ConfigurationSection lobby = lobbies.getConfigurationSection(lobbyId);
                if (lobby == null) continue;

                try {
                    String materialName = lobby.getString("icon.material", "GRASS_BLOCK");
                    Material material = Material.valueOf(materialName);
                    ItemStack icon = new ItemStack(material);
                    ItemMeta meta = icon.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(lobby.getString("display-name", lobbyId).replace("&", "§"));

                        List<String> lore = new ArrayList<>();
                        List<String> configLore = lobby.getStringList("description");
                        if (!configLore.isEmpty()) {
                            lore = configLore.stream()
                                    .map(line -> line.replace("&", "§"))
                                    .collect(Collectors.toList());
                        }

                        int currentPlayers = plugin.getLobbyManager().getLobbyPlayerCount(lobbyId);
                        int maxPlayers = lobby.getInt("max-players", 100);
                        lore.add("§7Oyuncular: §a" + currentPlayers + "§7/§c" + maxPlayers);

                        meta.setLore(lore);
                        icon.setItemMeta(meta);
                    }

                    int slot = lobby.getInt("slot", 0);
                    if (slot >= 0 && slot < size) {
                        inventory.setItem(slot, icon);
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Geçersiz materyal: " + lobbyId + " için!");
                }
            }
        }

        player.openInventory(inventory);
    }
}

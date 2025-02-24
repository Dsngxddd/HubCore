package net.cengiz1.lunehubcore.manager;

import net.cengiz1.lunehubcore.LuneHubCore;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class MenuManager {
    private final LuneHubCore plugin;
    private static final String DEFAULT_TITLE = "Menu";
    private static final int DEFAULT_SIZE = 27;
    private final Map<UUID, Long> lastMenuOpenTime;
    private final Map<UUID, String> openMenus;

    public MenuManager(LuneHubCore plugin) {
        this.plugin = plugin;
        this.lastMenuOpenTime = new HashMap<>();
        this.openMenus = new HashMap<>();
    }

    public void openServerSelector(Player player) {
        long currentTime = System.currentTimeMillis();
        long lastOpen = lastMenuOpenTime.getOrDefault(player.getUniqueId(), 0L);
        if (currentTime - lastOpen < 1000) {
            return;
        }

        lastMenuOpenTime.put(player.getUniqueId(), currentTime);

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
        openMenus.put(player.getUniqueId(), "server-selector");
        if (menuConfig.getBoolean("fill-empty", false)) {
            String emptyMaterial = menuConfig.getString("empty-material", "GRAY_STAINED_GLASS_PANE");
            Material fillerMaterial;
            try {
                fillerMaterial = Material.valueOf(emptyMaterial);
            } catch (IllegalArgumentException e) {
                fillerMaterial = Material.GRAY_STAINED_GLASS_PANE;
            }
            ItemStack filler = new ItemStack(fillerMaterial);
            ItemMeta fillerMeta = filler.getItemMeta();
            if (fillerMeta != null) {
                fillerMeta.setDisplayName(" ");
                filler.setItemMeta(fillerMeta);
            }

            for (int i = 0; i < size; i++) {
                inventory.setItem(i, filler);
            }
        }
        boolean autoRefresh = menuConfig.getBoolean("auto-refresh-player-count", true);
        if (autoRefresh) {
            long lastRefresh = lastMenuOpenTime.getOrDefault(player.getUniqueId(), 0L);
            if (currentTime - lastRefresh > 5000) {
                plugin.getServerManager().requestAllPlayerCounts(player);
            }
        }

        ConfigurationSection servers = plugin.getConfig().getConfigurationSection("servers");
        if (servers != null) {
            for (String serverId : servers.getKeys(false)) {
                ConfigurationSection server = servers.getConfigurationSection(serverId);
                if (server == null) continue;

                try {
                    if (!server.getBoolean("visible", true)) {
                        continue;
                    }
                    boolean isOnline = plugin.getServerManager().isServerOnline(serverId);
                    int playerCount = plugin.getServerManager().getServerPlayerCount(serverId);
                    String materialName;
                    if (isOnline) {
                        materialName = server.getString("icon.material", "GRASS_BLOCK");
                    } else {
                        materialName = server.getString("icon.offline-material",
                                server.getString("icon.material", "BARRIER"));
                    }

                    Material material = Material.valueOf(materialName);
                    ItemStack icon = new ItemStack(material);
                    ItemMeta meta = icon.getItemMeta();
                    if (meta != null) {
                        String displayName;
                        if (isOnline) {
                            displayName = server.getString("display-name", serverId);
                        } else {
                            displayName = server.getString("offline-display-name",
                                    "&c⚠ " + server.getString("display-name", serverId));
                        }
                        meta.setDisplayName(displayName.replace("&", "§"));
                        List<String> lore = new ArrayList<>();
                        List<String> configLore = server.getStringList("description");

                        if (!configLore.isEmpty()) {
                            for (String line : configLore) {
                                if (line.contains("%player_count%")) {
                                    if (playerCount >= 0) {
                                        line = line.replace("%player_count%", String.valueOf(playerCount));
                                    } else {
                                        line = line.replace("%player_count%", "?");
                                    }
                                }
                                lore.add(line.replace("&", "§"));
                            }
                        }
                        String statusFormat = plugin.getConfig().getString("menus.server-selector.status-format", "&8[%status%&8]");
                        String statusText;

                        if (isOnline) {
                            statusText = statusFormat.replace("%status%",
                                    plugin.getConfig().getString("messages.server-online", "&aAKTİF"));
                        } else {
                            statusText = statusFormat.replace("%status%",
                                    plugin.getConfig().getString("messages.server-offline", "&cKAPALI"));
                        }

                        lore.add(statusText.replace("&", "§"));
                        boolean globalRedirect = plugin.getConfig().getBoolean("settings.enable-server-redirect", true);
                        boolean serverRedirect = server.getBoolean("enable-redirect", true);

                        if (!globalRedirect || !serverRedirect) {
                            String infoMsg = server.getString("info-message",
                                            plugin.getConfig().getString("messages.server-info", "&eBu sunucuya gitmek için &b/server %server%&e komutunu kullanın."))
                                    .replace("%server%", server.getString("server-name", serverId))
                                    .replace("&", "§");
                            lore.add("");
                            lore.add(infoMsg);
                        }

                        meta.setLore(lore);
                        icon.setItemMeta(meta);
                    }
                    if (server.getBoolean("icon.glowing", false) && isOnline) {
                    }

                    int slot = server.getInt("slot", -1);
                    if (slot < 0 || slot >= size) {
                        for (int i = 0; i < size; i++) {
                            if (inventory.getItem(i) == null ||
                                    (inventory.getItem(i).getItemMeta() != null &&
                                            inventory.getItem(i).getItemMeta().getDisplayName().equals(" "))) {
                                slot = i;
                                break;
                            }
                        }
                    }

                    if (slot >= 0 && slot < size) {
                        inventory.setItem(slot, icon);
                    } else {
                        plugin.getLogger().warning(serverId + " için uygun slot bulunamadı!");
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Geçersiz materyal: " + serverId + " için!");
                }
            }
        }

        player.openInventory(inventory);
    }

    public void openLobbySelector(Player player) {
        long currentTime = System.currentTimeMillis();
        long lastOpen = lastMenuOpenTime.getOrDefault(player.getUniqueId(), 0L);
        if (currentTime - lastOpen < 1000) {
            return;
        }

        lastMenuOpenTime.put(player.getUniqueId(), currentTime);
        openMenus.put(player.getUniqueId(), "lobby-selector");

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
        if (menuConfig.getBoolean("fill-empty", false)) {
            String emptyMaterial = menuConfig.getString("empty-material", "GRAY_STAINED_GLASS_PANE");
            Material fillerMaterial;
            try {
                fillerMaterial = Material.valueOf(emptyMaterial);
            } catch (IllegalArgumentException e) {
                fillerMaterial = Material.GRAY_STAINED_GLASS_PANE;
            }
            ItemStack filler = new ItemStack(fillerMaterial);
            ItemMeta fillerMeta = filler.getItemMeta();
            if (fillerMeta != null) {
                fillerMeta.setDisplayName(" ");
                filler.setItemMeta(fillerMeta);
            }

            for (int i = 0; i < size; i++) {
                inventory.setItem(i, filler);
            }
        }

        ConfigurationSection lobbies = plugin.getConfig().getConfigurationSection("lobbies");
        if (lobbies != null) {
            for (String lobbyId : lobbies.getKeys(false)) {
                ConfigurationSection lobby = lobbies.getConfigurationSection(lobbyId);
                if (lobby == null) continue;

                try {
                    if (!lobby.getBoolean("visible", true)) {
                        continue;
                    }

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

                    int slot = lobby.getInt("slot", -1);
                    if (slot < 0 || slot >= size) {
                        for (int i = 0; i < size; i++) {
                            if (inventory.getItem(i) == null ||
                                    (inventory.getItem(i).getItemMeta() != null &&
                                            inventory.getItem(i).getItemMeta().getDisplayName().equals(" "))) {
                                slot = i;
                                break;
                            }
                        }
                    }

                    if (slot >= 0 && slot < size) {
                        inventory.setItem(slot, icon);
                    } else {
                        plugin.getLogger().warning(lobbyId + " için uygun slot bulunamadı!");
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Geçersiz materyal: " + lobbyId + " için!");
                }
            }
        }

        player.openInventory(inventory);
    }

    /**
     * @param player
     * @return
     */
    public String getOpenMenu(Player player) {
        return openMenus.get(player.getUniqueId());
    }

    /**
     * @param player
     */
    public void removeOpenMenu(Player player) {
        openMenus.remove(player.getUniqueId());
    }
}
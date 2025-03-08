package net.cengiz1.hubcore.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import net.cengiz1.hubcore.HubCore;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MenuManager {
    private final HubCore plugin;
    private static final String DEFAULT_TITLE = "Menu";
    private static final int DEFAULT_SIZE = 27;
    private final Map<UUID, Long> lastMenuOpenTime;
    private final Map<UUID, String> openMenus;

    public MenuManager(HubCore plugin) {
        this.plugin = plugin;
        this.lastMenuOpenTime = new HashMap<UUID, Long>();
        this.openMenus = new HashMap<UUID, String>();
    }

    public void openServerSelector(Player player) {
        ConfigurationSection servers;
        long lastRefresh;
        boolean autoRefresh;
        long lastOpen;
        long currentTime = System.currentTimeMillis();
        if (currentTime - (lastOpen = this.lastMenuOpenTime.getOrDefault(player.getUniqueId(), 0L).longValue()) < 1000L) {
            return;
        }
        this.lastMenuOpenTime.put(player.getUniqueId(), currentTime);
        if (this.plugin.getConfig() == null) {
            player.sendMessage("§cKonfig\u00fcrasyon y\u00fcklenemedi!");
            return;
        }
        ConfigurationSection menuConfig = this.plugin.getConfig().getConfigurationSection("menus.server-selector");
        if (menuConfig == null) {
            player.sendMessage("§cServer selector men\u00fcs\u00fc bulunamad\u0131!");
            return;
        }
        String title = menuConfig.getString("title", DEFAULT_TITLE).replace("&", "§");
        int size = menuConfig.getInt("size", 27);
        Inventory inventory = Bukkit.createInventory(null, (int)size, (String)title);
        this.openMenus.put(player.getUniqueId(), "server-selector");
        if (menuConfig.getBoolean("fill-empty", false)) {
            Material fillerMaterial;
            String emptyMaterial = menuConfig.getString("empty-material", "GRAY_STAINED_GLASS_PANE");
            int emptyCustomModelData = menuConfig.getInt("empty-custom-model-data", 0);
            try {
                fillerMaterial = Material.valueOf((String)emptyMaterial);
            } catch (IllegalArgumentException e) {
                fillerMaterial = Material.GRAY_STAINED_GLASS_PANE;
            }
            ItemStack filler = new ItemStack(fillerMaterial);
            ItemMeta fillerMeta = filler.getItemMeta();
            if (fillerMeta != null) {
                fillerMeta.setDisplayName(" ");
                // Add CustomModelData support for filler items
                if (emptyCustomModelData > 0) {
                    fillerMeta.setCustomModelData(emptyCustomModelData);
                }
                filler.setItemMeta(fillerMeta);
            }
            for (int i = 0; i < size; ++i) {
                inventory.setItem(i, filler);
            }
        }
        if ((autoRefresh = menuConfig.getBoolean("auto-refresh-player-count", true)) && currentTime - (lastRefresh = this.lastMenuOpenTime.getOrDefault(player.getUniqueId(), 0L).longValue()) > 5000L) {
            this.plugin.getServerManager().requestAllPlayerCounts(player);
        }
        if ((servers = this.plugin.getConfig().getConfigurationSection("servers")) != null) {
            for (String serverId : servers.getKeys(false)) {
                ConfigurationSection server = servers.getConfigurationSection(serverId);
                if (server == null) continue;
                try {
                    int slot;
                    if (!server.getBoolean("visible", true)) continue;
                    boolean isOnline = this.plugin.getServerManager().isServerOnline(serverId);
                    int playerCount = this.plugin.getServerManager().getServerPlayerCount(serverId);
                    String materialName = isOnline ? server.getString("icon.material", "GRASS_BLOCK") : server.getString("icon.offline-material", server.getString("icon.material", "BARRIER"));
                    Material material = Material.valueOf((String)materialName);
                    ItemStack icon = new ItemStack(material);
                    ItemMeta meta = icon.getItemMeta();
                    if (meta != null) {
                        String displayName = isOnline ? server.getString("display-name", serverId) : server.getString("offline-display-name", "&c\u26a0 " + server.getString("display-name", serverId));
                        meta.setDisplayName(displayName.replace("&", "§"));

                        // Add CustomModelData support
                        int customModelData = isOnline ?
                                server.getInt("icon.custom-model-data", 0) :
                                server.getInt("icon.offline-custom-model-data", server.getInt("icon.custom-model-data", 0));

                        if (customModelData > 0) {
                            meta.setCustomModelData(customModelData);
                        }

                        ArrayList<String> lore = new ArrayList<String>();
                        List<String> configLore = server.getStringList("description");
                        if (!configLore.isEmpty()) {
                            for (String line : configLore) {
                                if (line.contains("%player_count%")) {
                                    line = playerCount >= 0 ? line.replace("%player_count%", String.valueOf(playerCount)) : line.replace("%player_count%", "?");
                                }
                                lore.add(line.replace("&", "§"));
                            }
                        }
                        String statusFormat = this.plugin.getConfig().getString("menus.server-selector.status-format", "&8[%status%&8]");
                        String statusText = isOnline ? statusFormat.replace("%status%", this.plugin.getConfig().getString("messages.server-online", "&aAKT\u0130F")) : statusFormat.replace("%status%", this.plugin.getConfig().getString("messages.server-offline", "&cKAPALI"));
                        lore.add(statusText.replace("&", "§"));
                        boolean globalRedirect = this.plugin.getConfig().getBoolean("settings.enable-server-redirect", true);
                        boolean serverRedirect = server.getBoolean("enable-redirect", true);
                        if (!globalRedirect || !serverRedirect) {
                            String infoMsg = server.getString("info-message", this.plugin.getConfig().getString("messages.server-info", "&eBu sunucuya gitmek i\u00e7in &b/server %server%&e komutunu kullan\u0131n.")).replace("%server%", server.getString("server-name", serverId)).replace("&", "§");
                            lore.add("");
                            lore.add(infoMsg);
                        }
                        meta.setLore(lore);
                        icon.setItemMeta(meta);
                    }
                    if (!server.getBoolean("icon.glowing", false) || isOnline) {

                    }
                    if ((slot = server.getInt("slot", -1)) < 0 || slot >= size) {
                        for (int i = 0; i < size; ++i) {
                            if (inventory.getItem(i) != null && (inventory.getItem(i).getItemMeta() == null || !inventory.getItem(i).getItemMeta().getDisplayName().equals(" "))) continue;
                            slot = i;
                            break;
                        }
                    }
                    if (slot >= 0 && slot < size) {
                        inventory.setItem(slot, icon);
                        continue;
                    }
                } catch (IllegalArgumentException e) {
                }
            }
        }
        player.openInventory(inventory);
    }

    public void openLobbySelector(Player player) {
        ConfigurationSection lobbies;
        long lastOpen;
        long currentTime = System.currentTimeMillis();
        if (currentTime - (lastOpen = this.lastMenuOpenTime.getOrDefault(player.getUniqueId(), 0L).longValue()) < 1000L) {
            return;
        }
        this.lastMenuOpenTime.put(player.getUniqueId(), currentTime);
        this.openMenus.put(player.getUniqueId(), "lobby-selector");
        if (this.plugin.getConfig() == null) {
            player.sendMessage("§cConfig yüklenemedi");
            return;
        }
        ConfigurationSection menuConfig = this.plugin.getConfig().getConfigurationSection("menus.lobby-selector");
        if (menuConfig == null) {
            player.sendMessage("§cLobby selector menü bulunamadı!");
            return;
        }
        String title = menuConfig.getString("title", DEFAULT_TITLE).replace("&", "§");
        int size = menuConfig.getInt("size", 27);
        Inventory inventory = Bukkit.createInventory(null, (int)size, (String)title);
        if (menuConfig.getBoolean("fill-empty", false)) {
            Material fillerMaterial;
            String emptyMaterial = menuConfig.getString("empty-material", "GRAY_STAINED_GLASS_PANE");
            int emptyCustomModelData = menuConfig.getInt("empty-custom-model-data", 0);
            try {
                fillerMaterial = Material.valueOf((String)emptyMaterial);
            } catch (IllegalArgumentException e) {
                fillerMaterial = Material.GRAY_STAINED_GLASS_PANE;
            }
            ItemStack filler = new ItemStack(fillerMaterial);
            ItemMeta fillerMeta = filler.getItemMeta();
            if (fillerMeta != null) {
                fillerMeta.setDisplayName(" ");
                // Add CustomModelData for filler items
                if (emptyCustomModelData > 0) {
                    fillerMeta.setCustomModelData(emptyCustomModelData);
                }
                filler.setItemMeta(fillerMeta);
            }
            for (int i = 0; i < size; ++i) {
                inventory.setItem(i, filler);
            }
        }
        if ((lobbies = this.plugin.getConfig().getConfigurationSection("lobbies")) != null) {
            for (String lobbyId : lobbies.getKeys(false)) {
                ConfigurationSection lobby = lobbies.getConfigurationSection(lobbyId);
                if (lobby == null) continue;
                try {
                    int slot;
                    if (!lobby.getBoolean("visible", true)) continue;
                    String materialName = lobby.getString("icon.material", "GRASS_BLOCK");
                    Material material = Material.valueOf((String)materialName);
                    ItemStack icon = new ItemStack(material);
                    ItemMeta meta = icon.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(lobby.getString("display-name", lobbyId).replace("&", "§"));

                        // Add CustomModelData support
                        int customModelData = lobby.getInt("icon.custom-model-data", 0);
                        if (customModelData > 0) {
                            meta.setCustomModelData(customModelData);
                        }

                        List<String> lore = new ArrayList<String>();
                        List<String> configLore = lobby.getStringList("description");
                        if (!configLore.isEmpty()) {
                            lore = configLore.stream()
                                    .map(line -> line.replace("&", "§"))
                                    .collect(Collectors.toList());
                        }
                        int currentPlayers = this.plugin.getLobbyManager().getLobbyPlayerCount(lobbyId);
                        int maxPlayers = lobby.getInt("max-players", 100);
                        lore.add("§Online: §a" + currentPlayers + "§7/§c" + maxPlayers);
                        meta.setLore(lore);
                        icon.setItemMeta(meta);
                    }
                    if ((slot = lobby.getInt("slot", -1)) < 0 || slot >= size) {
                        for (int i = 0; i < size; ++i) {
                            if (inventory.getItem(i) != null && (inventory.getItem(i).getItemMeta() == null || !inventory.getItem(i).getItemMeta().getDisplayName().equals(" "))) continue;
                            slot = i;
                            break;
                        }
                    }
                    if (slot >= 0 && slot < size) {
                        inventory.setItem(slot, icon);
                        continue;
                    }
                } catch (IllegalArgumentException e) {
                    this.plugin.getLogger().warning("§eGeçersiz materyal: " + lobbyId);
                }
            }
        }
        player.openInventory(inventory);
    }

    public String getOpenMenu(Player player) {
        return this.openMenus.get(player.getUniqueId());
    }

    public void removeOpenMenu(Player player) {
        this.openMenus.remove(player.getUniqueId());
    }
}
package net.cengiz1.hubcore.manager;

import net.cengiz1.hubcore.HubCore;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ItemManager {
    private final HubCore plugin;

    public ItemManager(HubCore plugin) {
        this.plugin = plugin;
    }

    public void giveLobbyItems(Player player) {
        try {

            // Envanteri temizle
            clearInventory(player);

            // Öğe bölümünü al
            ConfigurationSection items = plugin.getConfig().getConfigurationSection("items");
            if (items == null) {
                return;
            }

            for (String itemId : items.getKeys(false)) {
                ConfigurationSection itemSection = items.getConfigurationSection(itemId);
                if (itemSection == null) continue;

                String permission = itemSection.getString("permission", "");
                if (!permission.isEmpty() && !player.hasPermission(permission)) continue;

                String materialName = itemSection.getString("material");
                if (materialName == null) {
                    continue;
                }

                Material material;
                try {
                    material = Material.valueOf(materialName);
                } catch (IllegalArgumentException e) {
                    continue;
                }

                ItemStack item = new ItemStack(material);
                ItemMeta meta = item.getItemMeta();

                if (meta != null) {
                    // Öğe adı
                    String displayName = itemSection.getString("display-name");
                    if (displayName != null) {
                        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
                    }

                    // CustomModelData
                    if (itemSection.contains("custom-model-data")) {
                        int customModelData = itemSection.getInt("custom-model-data");
                        if (customModelData > 0) {
                            meta.setCustomModelData(customModelData);
                        }
                    }

                    // Lore (açıklama)
                    List<String> loreTmp = itemSection.getStringList("lore");
                    if (!loreTmp.isEmpty()) {
                        List<String> lore = new ArrayList<>();
                        for (String line : loreTmp) {
                            lore.add(ChatColor.translateAlternateColorCodes('&', line));
                        }
                        meta.setLore(lore);
                    }

                    item.setItemMeta(meta);
                }

                int slot = itemSection.getInt("slot", 0);
                player.getInventory().setItem(slot, item);
            }

            // Envanteri güncelle
            player.updateInventory();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void clearInventory(Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
    }

    /**
     * Creates an ItemStack with CustomModelData support
     *
     * @param material Material of the item
     * @param displayName Display name of the item
     * @param lore Lore lines of the item
     * @param customModelData CustomModelData value (0 for none)
     * @return The created ItemStack
     */
    public ItemStack createItem(Material material, String displayName, List<String> lore, int customModelData) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(displayName);

            if (customModelData > 0) {
                meta.setCustomModelData(customModelData);
            }

            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }
}
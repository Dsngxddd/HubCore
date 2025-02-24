package net.cengiz1.lunehubcore.manager;

import net.cengiz1.lunehubcore.LuneHubCore;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.List;
import java.util.stream.Collectors;

public class ItemManager {
    private final LuneHubCore plugin;

    public ItemManager(LuneHubCore plugin) {
        this.plugin = plugin;
    }

    public void giveLobbyItems(Player player) {
        clearInventory(player);

        ConfigurationSection items = plugin.getConfig().getConfigurationSection("items");
        if (items == null) return;

        for (String itemId : items.getKeys(false)) {
            ConfigurationSection itemSection = items.getConfigurationSection(itemId);
            if (itemSection == null) continue;
            String permission = itemSection.getString("permission", "");
            if (!permission.isEmpty() && !player.hasPermission(permission)) continue;

            ItemStack item = new ItemStack(Material.valueOf(itemSection.getString("material")));
            ItemMeta meta = item.getItemMeta();

            meta.setDisplayName(itemSection.getString("display-name").replace("&", "§"));

            List<String> lore = itemSection.getStringList("lore").stream()
                    .map(line -> line.replace("&", "§"))
                    .collect(Collectors.toList());
            meta.setLore(lore);

            item.setItemMeta(meta);

            int slot = itemSection.getInt("slot", 0);
            player.getInventory().setItem(slot, item);
        }
    }

    private void clearInventory(Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
    }
}
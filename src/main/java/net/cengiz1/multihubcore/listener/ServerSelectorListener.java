package net.cengiz1.multihubcore.listener;

import net.cengiz1.multihubcore.MultiHubCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class ServerSelectorListener implements Listener {
    private final MultiHubCore plugin;

    public ServerSelectorListener(MultiHubCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onServerSelectorUse(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null) return;

        String selectorName = plugin.getConfig().getString("items.server-selector.display-name").replace("&", "§");

        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName() &&
                item.getItemMeta().getDisplayName().equals(selectorName)) {

            event.setCancelled(true);
            plugin.getMenuManager().openServerSelector(event.getPlayer());
        }
    }
}
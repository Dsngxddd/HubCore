package net.cengiz1.hubcore.listener;

import net.cengiz1.hubcore.HubCore;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

public class ProtectionListener
implements Listener {
    private final HubCore plugin;

    public ProtectionListener(HubCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!this.plugin.getConfig().getBoolean("settings.allow-block-break") && !event.getPlayer().hasPermission("hubcore.build")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!this.plugin.getConfig().getBoolean("settings.allow-block-place") && !event.getPlayer().hasPermission("hubcore.build")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && !this.plugin.getConfig().getBoolean("settings.allow-pvp")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (this.plugin.getConfig().getBoolean("settings.disable-hunger")) {
            event.setCancelled(true);
            if (event.getEntity() instanceof Player) {
                ((Player)event.getEntity()).setFoodLevel(20);
            }
        }
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        if (this.plugin.getConfig().getBoolean("settings.disable-weather") && event.toWeatherState()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        if (!event.getPlayer().hasPermission("hubcore.dropitem")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!this.plugin.getConfig().getBoolean("settings.disable-block-interactions")) {
            return;
        }
        Player player = event.getPlayer();
        if (player.hasPermission("hubcore.build") || player.hasPermission("hubcore.interact")) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }
        Material type = clickedBlock.getType();
        if (type.name().contains("DOOR") || type.name().contains("GATE") || type.name().contains("TRAPDOOR") || type.name().contains("FENCE_GATE")) {
            event.setCancelled(true);
            return;
        }
        if (type.name().contains("BUTTON") || type == Material.LEVER) {
            event.setCancelled(true);
            return;
        }
        if (type.name().contains("BED")) {
            event.setCancelled(true);
            return;
        }
        if (type.name().contains("CHEST") || type.name().contains("BARREL") || type.name().contains("SHULKER") || type == Material.FURNACE || type == Material.BLAST_FURNACE || type == Material.SMOKER || type == Material.BREWING_STAND || type == Material.ENCHANTING_TABLE || type == Material.ANVIL || type == Material.HOPPER || type == Material.DISPENSER || type == Material.DROPPER) {
            event.setCancelled(true);
            return;
        }
        if (type == Material.REPEATER || type == Material.COMPARATOR || type == Material.DAYLIGHT_DETECTOR || type == Material.FLOWER_POT || type == Material.RESPAWN_ANCHOR || type == Material.BELL || type == Material.JUKEBOX || type == Material.NOTE_BLOCK) {
            event.setCancelled(true);
        }
    }
}


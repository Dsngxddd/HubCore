package net.cengiz1.multihubcore.listener;

import net.cengiz1.multihubcore.MultiHubCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

public class LobbyListener implements Listener {
    private final MultiHubCore plugin;

    public LobbyListener(MultiHubCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        if (!plugin.getConfig().getBoolean("settings.double-jump.enabled")) return;

        if (event.isFlying()) {
            event.setCancelled(true);
            Vector velocity = event.getPlayer().getLocation().getDirection().multiply(1.5);
            velocity.setY(1);
            event.getPlayer().setVelocity(velocity);
            if (plugin.getConfig().getBoolean("effects.double-jump.enabled")) {
                event.getPlayer().playSound(event.getPlayer().getLocation(),
                        plugin.getConfig().getString("effects.double-jump.sound"),
                        1.0f, 1.0f);
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!plugin.getConfig().getBoolean("settings.double-jump.enabled")) return;

        if (event.getPlayer().isOnGround()) {
            event.getPlayer().setAllowFlight(true);
        }
    }
}

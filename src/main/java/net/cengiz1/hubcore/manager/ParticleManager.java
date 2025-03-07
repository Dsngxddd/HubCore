package net.cengiz1.hubcore.manager;

import net.cengiz1.hubcore.HubCore;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ParticleManager {
    private final HubCore plugin;
    private final Map<UUID, Integer> playerEffects;

    public ParticleManager(HubCore plugin) {
        this.plugin = plugin;
        this.playerEffects = new HashMap<>();
    }

    public void startPlayerEffects(Player player) {
        String lobbyId = plugin.getLobbyManager().getPlayerLobby(player);
        if (!plugin.getConfig().getBoolean("lobbies." + lobbyId + ".particle-effects.enabled")) {
            return;
        }

        stopPlayerEffects(player);

        String effectType = plugin.getConfig().getString("lobbies." + lobbyId + ".particle-effects.type");
        int interval = plugin.getConfig().getInt("lobbies." + lobbyId + ".particle-effects.interval");

        int taskId = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline()) {
                stopPlayerEffects(player);
                return;
            }

            Location loc = player.getLocation().add(0, 2, 0);
            player.getWorld().spawnParticle(Particle.valueOf(effectType), loc, 10, 0.5, 0.5, 0.5, 0);
        }, 0L, interval).getTaskId();

        playerEffects.put(player.getUniqueId(), taskId);
    }

    public void stopPlayerEffects(Player player) {
        Integer taskId = playerEffects.remove(player.getUniqueId());
        if (taskId != null) {
            plugin.getServer().getScheduler().cancelTask(taskId);
        }
    }
}
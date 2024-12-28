package net.cengiz1.multihubcore.manager;

import net.cengiz1.multihubcore.MultiHubCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LobbyManager {
    private final MultiHubCore plugin;
    private final Map<String, Location> lobbySpawns;
    private final Map<UUID, String> playerLobby;

    public LobbyManager(MultiHubCore plugin) {
        this.plugin = plugin;
        this.lobbySpawns = new HashMap<>();
        this.playerLobby = new HashMap<>();
        loadLobbySpawns();
    }

    private void loadLobbySpawns() {
        ConfigurationSection lobbies = plugin.getConfig().getConfigurationSection("lobbies");
        if (lobbies != null) {
            for (String lobbyId : lobbies.getKeys(false)) {
                ConfigurationSection spawnSection = lobbies.getConfigurationSection(lobbyId + ".spawn");
                if (spawnSection != null) {
                    Location spawn = new Location(
                            plugin.getServer().getWorld(spawnSection.getString("world")),
                            spawnSection.getDouble("x"),
                            spawnSection.getDouble("y"),
                            spawnSection.getDouble("z"),
                            (float) spawnSection.getDouble("yaw"),
                            (float) spawnSection.getDouble("pitch")
                    );
                    lobbySpawns.put(lobbyId, spawn);
                }
            }
        }
    }

    public void teleportToLobby(Player player, String lobbyId) {
        // Eğer lobi yerel sunucudaysa
        Location spawn = lobbySpawns.get(lobbyId);
        if (spawn != null) {
            String permission = plugin.getConfig().getString("lobbies." + lobbyId + ".permission", "");
            if (!permission.isEmpty() && !player.hasPermission(permission)) {
                String noPermMsg = plugin.getConfig().getString("messages.no-permission")
                        .replace("&", "§");
                player.sendMessage(noPermMsg);
                return;
            }
            int maxPlayers = plugin.getConfig().getInt("lobbies." + lobbyId + ".max-players");
            if (getLobbyPlayerCount(lobbyId) >= maxPlayers) {
                String fullMsg = plugin.getConfig().getString("messages.lobby-full")
                        .replace("&", "§");
                player.sendMessage(fullMsg);
                return;
            }
            player.teleport(spawn);
            playerLobby.put(player.getUniqueId(), lobbyId);
            plugin.getItemManager().giveLobbyItems(player);
            plugin.getScoreboardManager().updateScoreboard(player);
            if (plugin.getConfig().getBoolean("lobbies." + lobbyId + ".particle-effects.enabled")) {
                plugin.getParticleManager().startPlayerEffects(player);
            }
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

            String welcomeMsg = plugin.getConfig().getString("messages.lobby-welcome")
                    .replace("%player%", player.getName())
                    .replace("%lobby%", plugin.getConfig().getString("lobbies." + lobbyId + ".display-name"))
                    .replace("&", "§");
            player.sendMessage(welcomeMsg);
            return;
        }

        // Eğer lobi başka bir sunucudaysa
        String serverName = plugin.getConfig().getString("lobbies." + lobbyId + ".server-name");
        if (serverName != null && !serverName.isEmpty()) {
            sendPlayerToServer(player, serverName);
        } else {
            String errorMsg = plugin.getConfig().getString("messages.lobby-not-found")
                    .replace("&", "§");
            player.sendMessage(errorMsg);
        }
    }

    private void sendPlayerToServer(Player player, String serverName) {
        try {
            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(byteArray);
            out.writeUTF("Connect");
            out.writeUTF(serverName);

            player.sendPluginMessage(plugin, "BungeeCord", byteArray.toByteArray());
            player.sendMessage("Sunucuya ışınlanıyorsunuz: " + serverName); // Hata ayıklama amaçlı mesaj
        } catch (Exception e) {
            Bukkit.getLogger().severe("Oyuncuyu başka bir sunucuya gönderirken hata oluştu: " + e.getMessage());
            player.sendMessage("Sunucuya bağlanılamadı.");
        }
    }

    public void setLobbySpawn(String lobbyId, Location location) {
        lobbySpawns.put(lobbyId, location);

        ConfigurationSection lobbySection = plugin.getConfig().getConfigurationSection("lobbies." + lobbyId);
        if (lobbySection != null) {
            lobbySection.set("spawn.world", location.getWorld().getName());
            lobbySection.set("spawn.x", location.getX());
            lobbySection.set("spawn.y", location.getY());
            lobbySection.set("spawn.z", location.getZ());
            lobbySection.set("spawn.yaw", location.getYaw());
            lobbySection.set("spawn.pitch", location.getPitch());
            plugin.saveConfig();
        }
    }

    public String getPlayerLobby(Player player) {
        return playerLobby.getOrDefault(player.getUniqueId(),
                plugin.getConfig().getString("settings.default-lobby"));
    }

    public void removePlayer(Player player) {
        playerLobby.remove(player.getUniqueId());
    }

    public int getLobbyPlayerCount(String lobbyId) {
        return (int) playerLobby.values().stream()
                .filter(lobby -> lobby.equals(lobbyId))
                .count();
    }
}
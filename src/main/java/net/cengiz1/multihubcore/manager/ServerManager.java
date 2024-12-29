package net.cengiz1.multihubcore.manager;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.cengiz1.multihubcore.MultiHubCore;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ServerManager {
    private final MultiHubCore plugin;
    private final Map<String, Boolean> serverStatus;

    public ServerManager(MultiHubCore plugin) {
        this.plugin = plugin;
        this.serverStatus = new HashMap<>();

        // BungeeCord kanalını kaydet
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");

        // Sunucu durumlarını periyodik olarak güncelle
        startStatusUpdater();
    }

    public void connectToServer(Player player, String serverId) {
        ConfigurationSection serverConfig = plugin.getConfig().getConfigurationSection("servers." + serverId);
        if (serverConfig == null) {
            player.sendMessage("§cSunucu bulunamadı!");
            return;
        }

        String serverName = serverConfig.getString("server-name");
        if (serverName == null || serverName.isEmpty()) {
            player.sendMessage("§cSunucu adı yapılandırılmamış!");
            return;
        }

        // Sunucu aktif mi kontrol et
        if (!isServerOnline(serverId)) {
            player.sendMessage("§cBu sunucu şu anda aktif değil!");
            return;
        }

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(serverName);

        try {
            player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
            player.sendMessage("§aSunucuya aktarılıyorsunuz: " + serverName);
        } catch (Exception e) {
            player.sendMessage("§cSunucuya aktarılırken bir hata oluştu!");
            plugin.getLogger().warning("Sunucu aktarma hatası: " + e.getMessage());
        }
    }

    private void startStatusUpdater() {
        new BukkitRunnable() {
            @Override
            public void run() {
                updateServerStatuses();
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 20L * 30); // Her 30 saniyede bir güncelle
    }

    private void updateServerStatuses() {
        ConfigurationSection serversSection = plugin.getConfig().getConfigurationSection("servers");
        if (serversSection == null) return;

        for (String serverId : serversSection.getKeys(false)) {
            String host = plugin.getConfig().getString("servers." + serverId + ".address");
            int port = plugin.getConfig().getInt("servers." + serverId + ".port", 25565);

            // Host null ise bu sunucuyu atla
            if (host == null || host.isEmpty()) {
                plugin.getLogger().warning("Sunucu " + serverId + " için adres tanımlanmamış!");
                continue;
            }

            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(host, port), 1000);
                serverStatus.put(serverId, true);
            } catch (Exception e) {
                serverStatus.put(serverId, false);
            }
        }
    }

    public boolean isServerOnline(String serverId) {
        return serverStatus.getOrDefault(serverId, false);
    }
}

class ServerInfo {
    private final String id;
    private final String displayName;
    private final String address;
    private final int port;
    private final int slot;
    private final String permission;
    private boolean online;

    public ServerInfo(String id, String displayName, String address, int port, int slot, String permission) {
        this.id = id;
        this.displayName = displayName;
        this.address = address;
        this.port = port;
        this.slot = slot;
        this.permission = permission;
        this.online = false;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getAddress() { return address; }
    public int getPort() { return port; }
    public int getSlot() { return slot; }
    public String getPermission() { return permission; }
    public boolean isOnline() { return online; }
    public void setOnline(boolean online) { this.online = online; }
}
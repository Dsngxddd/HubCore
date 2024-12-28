package net.cengiz1.multihubcore.manager;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.cengiz1.multihubcore.MultiHubCore;
import org.bukkit.entity.Player;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;

public class ServerManager {
    private final MultiHubCore plugin;
    private final Map<String, ServerInfo> servers;

    public ServerManager(MultiHubCore plugin) {
        this.plugin = plugin;
        this.servers = new HashMap<>();
        loadServers();
        int interval = plugin.getConfig().getInt("settings.server-check-interval");
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin,
                this::updateServerStatuses, 20L, interval);
    }

    private void loadServers() {
        ConfigurationSection serversSection = plugin.getConfig().getConfigurationSection("servers");
        if (serversSection != null) {
            for (String serverId : serversSection.getKeys(false)) {
                ConfigurationSection serverConfig = serversSection.getConfigurationSection(serverId);
                if (serverConfig != null) {
                    ServerInfo serverInfo = new ServerInfo(
                            serverId,
                            serverConfig.getString("display-name"),
                            serverConfig.getString("address"),
                            serverConfig.getInt("port"),
                            serverConfig.getInt("slot"),
                            serverConfig.getString("permission", "")
                    );
                    servers.put(serverId, serverInfo);
                }
            }
        }
    }

    public void connectToServer(Player player, String serverId) {
        ServerInfo server = servers.get(serverId);
        if (server == null) return;
        if (!server.getPermission().isEmpty() && !player.hasPermission(server.getPermission())) {
            String noPermMsg = plugin.getConfig().getString("messages.no-permission")
                    .replace("&", "§");
            player.sendMessage(noPermMsg);
            return;
        }
        if (!server.isOnline()) {
            String offlineMsg = plugin.getConfig().getString("messages.server-offline")
                    .replace("&", "§");
            player.sendMessage(offlineMsg);
            return;
        }
        if (plugin.getConfig().getBoolean("effects.switch.enabled")) {
            player.playSound(player.getLocation(),
                    plugin.getConfig().getString("effects.switch.sound"),
                    (float) plugin.getConfig().getDouble("effects.switch.volume"),
                    (float) plugin.getConfig().getDouble("effects.switch.pitch"));
        }
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(serverId);
        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
        String connectMsg = plugin.getConfig().getString("messages.server-connect")
                .replace("%server%", server.getDisplayName())
                .replace("&", "§");
        player.sendMessage(connectMsg);
    }

    private void updateServerStatuses() {
        for (ServerInfo server : servers.values()) {
            try {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(server.getAddress(), server.getPort()), 1000);
                socket.close();
                server.setOnline(true);
            } catch (IOException e) {
                server.setOnline(false);
            }
        }
    }

    public ServerInfo getServerInfo(String serverId) {
        return servers.get(serverId);
    }

    public Map<String, ServerInfo> getServers() {
        return new HashMap<>(servers);
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
package net.cengiz1.lunehubcore.manager;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.cengiz1.lunehubcore.LuneHubCore;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ServerManager {
    private final LuneHubCore plugin;
    private final Map<String, Boolean> serverStatus;
    private final Map<String, Integer> serverPlayerCounts;
    private final Map<String, Player> playerRequests;
    private boolean updatingPlayerCounts = false;
    private boolean needsMenuRefresh = false;

    public ServerManager(LuneHubCore plugin) {
        this.plugin = plugin;
        this.serverStatus = new HashMap<>();
        this.serverPlayerCounts = new HashMap<>();
        this.playerRequests = new HashMap<>();

        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, "BungeeCord", (channel, player, message) -> {
            if (!channel.equals("BungeeCord")) return;

            try {
                ByteArrayDataInput in = ByteStreams.newDataInput(message);
                String subchannel = in.readUTF();

                if (subchannel.equals("PlayerCount")) {
                    String server = in.readUTF();
                    int playerCount = in.readInt();
                    serverPlayerCounts.put(server, playerCount);
                    Player requester = playerRequests.get(server);
                    if (requester != null && requester.isOnline()) {
                        needsMenuRefresh = true;
                        playerRequests.remove(server);
                        if (playerRequests.isEmpty() && needsMenuRefresh && requester.isOnline()) {
                            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                                if (requester.isOnline()) {
                                    plugin.getMenuManager().openServerSelector(requester);
                                    updatingPlayerCounts = false;
                                    needsMenuRefresh = false;
                                }
                            }, 5L);
                        }
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning("BungeeCord mesajı işlenirken hata: " + e.getMessage());
            }
        });

        startStatusUpdater();
    }

    public void connectToServer(Player player, String serverId) {
        boolean globalRedirectEnabled = plugin.getConfig().getBoolean("settings.enable-server-redirect", true);

        ConfigurationSection serverConfig = plugin.getConfig().getConfigurationSection("servers." + serverId);
        if (serverConfig == null) {
            player.sendMessage("§cSunucu bulunamadı!");
            return;
        }
        boolean serverRedirectEnabled = serverConfig.getBoolean("enable-redirect", true);
        boolean useCommand = serverConfig.getBoolean("use-command", false);
        String serverName = serverConfig.getString("server-name");

        if (serverName == null || serverName.isEmpty()) {
            player.sendMessage("§cSunucu adı yapılandırılmamış!");
            return;
        }

        if (!isServerOnline(serverId)) {
            String offlineMsg = plugin.getConfig().getString("messages.server-offline", "§cBu sunucu şu anda aktif değil!");
            player.sendMessage(offlineMsg);
            return;
        }

        if (!globalRedirectEnabled || !serverRedirectEnabled) {
            String infoMessage = serverConfig.getString("info-message",
                    plugin.getConfig().getString("messages.server-info", "&eBu sunucuya gitmek için &b/queue %server%&e komutunu kullanın.")
                            .replace("%server%", serverName)
                            .replace("&", "§"));
            player.sendMessage(infoMessage);
            return;
        }

        if (useCommand) {
            String command = serverConfig.getString("command", "queue %server%");
            command = command.replace("%server%", serverName);

            String commandMsg = plugin.getConfig().getString("messages.execute-command", "&aKomut çalıştırılıyor: %command%")
                    .replace("%command%", command)
                    .replace("&", "§");
            player.sendMessage(commandMsg);
            Bukkit.dispatchCommand(player, command);
        } else {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Connect");
            out.writeUTF(serverName);

            try {
                player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
                String transferMsg = plugin.getConfig().getString("messages.server-transfer", "§aSunucuya aktarılıyorsunuz: %server%")
                        .replace("%server%", serverName)
                        .replace("&", "§");
                player.sendMessage(transferMsg);
            } catch (Exception e) {
                player.sendMessage("§cSunucuya aktarılırken bir hata oluştu!");
                plugin.getLogger().warning("Sunucu aktarma hatası: " + e.getMessage());
            }
        }
    }

    private void startStatusUpdater() {
        new BukkitRunnable() {
            @Override
            public void run() {
                updateServerStatuses();
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 20L * 30);
    }

    private void updateServerStatuses() {
        ConfigurationSection serversSection = plugin.getConfig().getConfigurationSection("servers");
        if (serversSection == null) return;

        for (String serverId : serversSection.getKeys(false)) {
            String host = plugin.getConfig().getString("servers." + serverId + ".address");
            int port = plugin.getConfig().getInt("servers." + serverId + ".port", 25565);
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

    /**

     *
     * @param serverId
     * @return
     */
    public int getServerPlayerCount(String serverId) {
        String serverName = plugin.getConfig().getString("servers." + serverId + ".server-name");
        if (serverName == null) return -1;

        return serverPlayerCounts.getOrDefault(serverName, -1);
    }

    /**
     *
     * @param player
     * @param serverId
     */
    public void requestPlayerCount(Player player, String serverId) {
        String serverName = plugin.getConfig().getString("servers." + serverId + ".server-name");
        if (serverName == null) return;
        playerRequests.put(serverName, player);

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("PlayerCount");
        out.writeUTF(serverName);

        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
    }

    /**
     * @param player
     */
    public void requestAllPlayerCounts(Player player) {
        if (updatingPlayerCounts) {
            return;
        }

        ConfigurationSection serversSection = plugin.getConfig().getConfigurationSection("servers");
        if (serversSection == null) return;
        updatingPlayerCounts = true;
        needsMenuRefresh = false;
        playerRequests.clear();

        for (String serverId : serversSection.getKeys(false)) {
            requestPlayerCount(player, serverId);
        }
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (updatingPlayerCounts) {
                playerRequests.clear();
                updatingPlayerCounts = false;
                needsMenuRefresh = false;
            }
        }, 20L * 30);
    }
}
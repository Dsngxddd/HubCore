package net.cengiz1.hubcore.manager;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import net.cengiz1.hubcore.HubCore;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class ServerManager {
    private final HubCore plugin;
    private final Map<String, Boolean> serverStatus;
    private final Map<String, Integer> serverPlayerCounts;
    private final Map<String, Player> playerRequests;
    private boolean updatingPlayerCounts = false;
    private boolean needsMenuRefresh = false;

    public ServerManager(HubCore plugin) {
        this.plugin = plugin;
        this.serverStatus = new HashMap<String, Boolean>();
        this.serverPlayerCounts = new HashMap<String, Integer>();
        this.playerRequests = new HashMap<String, Player>();
        plugin.getServer().getMessenger().registerOutgoingPluginChannel((Plugin)plugin, "BungeeCord");
        plugin.getServer().getMessenger().registerIncomingPluginChannel((Plugin)plugin, "BungeeCord", (channel, player, message) -> {
            if (!channel.equals("BungeeCord")) {
                return;
            }
            try {
                ByteArrayDataInput in = ByteStreams.newDataInput(message);
                String subchannel = in.readUTF();
                if (subchannel.equals("PlayerCount")) {
                    String server = in.readUTF();
                    int playerCount = in.readInt();
                    this.serverPlayerCounts.put(server, playerCount);
                    Player requester = this.playerRequests.get(server);
                    if (requester != null && requester.isOnline()) {
                        this.needsMenuRefresh = true;
                        this.playerRequests.remove(server);
                        if (this.playerRequests.isEmpty() && this.needsMenuRefresh && requester.isOnline()) {
                            plugin.getServer().getScheduler().runTaskLater((Plugin)plugin, () -> {
                                if (requester.isOnline()) {
                                    plugin.getMenuManager().openServerSelector(requester);
                                    this.updatingPlayerCounts = false;
                                    this.needsMenuRefresh = false;
                                }
                            }, 5L);
                        }
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning("BungeeCord mesaj\u0131 i\u015flenirken hata: " + e.getMessage());
            }
        });
        this.startStatusUpdater();
    }

    public void connectToServer(Player player, String serverId) {
        boolean globalRedirectEnabled = this.plugin.getConfig().getBoolean("settings.enable-server-redirect", true);
        ConfigurationSection serverConfig = this.plugin.getConfig().getConfigurationSection("servers." + serverId);
        if (serverConfig == null) {
            player.sendMessage("§cSunucu bulunamad\u0131!");
            return;
        }
        boolean serverRedirectEnabled = serverConfig.getBoolean("enable-redirect", true);
        boolean useCommand = serverConfig.getBoolean("use-command", false);
        String serverName = serverConfig.getString("server-name");
        if (serverName == null || serverName.isEmpty()) {
            player.sendMessage("§cSunucu ad\u0131 yap\u0131land\u0131r\u0131lmam\u0131\u015f!");
            return;
        }
        if (!this.isServerOnline(serverId)) {
            String offlineMsg = this.plugin.getConfig().getString("messages.server-offline", "§cBu sunucu \u015fu anda aktif de\u011fil!");
            player.sendMessage(offlineMsg);
            return;
        }
        if (!globalRedirectEnabled || !serverRedirectEnabled) {
            String infoMessage = serverConfig.getString("info-message", this.plugin.getConfig().getString("messages.server-info", "&eBu sunucuya gitmek i\u00e7in &b/queue %server%&e komutunu kullan\u0131n.").replace("%server%", serverName).replace("&", "§"));
            player.sendMessage(infoMessage);
            return;
        }
        if (useCommand) {
            String command = serverConfig.getString("command", "queue %server%");
            command = command.replace("%server%", serverName);
            String commandMsg = this.plugin.getConfig().getString("messages.execute-command", "&aKomut \u00e7al\u0131\u015ft\u0131r\u0131l\u0131yor: %command%").replace("%command%", command).replace("&", "§");
            player.sendMessage(commandMsg);
            Bukkit.dispatchCommand((CommandSender)player, (String)command);
        } else {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Connect");
            out.writeUTF(serverName);
            try {
                player.sendPluginMessage((Plugin)this.plugin, "BungeeCord", out.toByteArray());
                String transferMsg = this.plugin.getConfig().getString("messages.server-transfer", "§aSunucuya aktar\u0131l\u0131yorsunuz: %server%").replace("%server%", serverName).replace("&", "§");
                player.sendMessage(transferMsg);
            } catch (Exception e) {
                player.sendMessage("§cSunucuya aktar\u0131l\u0131rken bir hata olu\u015ftu!");
                this.plugin.getLogger().warning("Sunucu aktarma hatas\u0131: " + e.getMessage());
            }
        }
    }

    private void startStatusUpdater() {
        new BukkitRunnable(){

            public void run() {
                ServerManager.this.updateServerStatuses();
            }
        }.runTaskTimerAsynchronously((Plugin)this.plugin, 0L, 600L);
    }

    private void updateServerStatuses() {
        ConfigurationSection serversSection = this.plugin.getConfig().getConfigurationSection("servers");
        if (serversSection == null) {
            return;
        }
        for (String serverId : serversSection.getKeys(false)) {
            String host = this.plugin.getConfig().getString("servers." + serverId + ".address");
            int port = this.plugin.getConfig().getInt("servers." + serverId + ".port", 25565);
            if (host == null || host.isEmpty()) {
                this.plugin.getLogger().warning("Sunucu " + serverId + " i\u00e7in adres tan\u0131mlanmam\u0131\u015f!");
                continue;
            }
            try (Socket socket = new Socket();){
                socket.connect(new InetSocketAddress(host, port), 1000);
                this.serverStatus.put(serverId, true);
            } catch (Exception e) {
                this.serverStatus.put(serverId, false);
            }
        }
    }

    public boolean isServerOnline(String serverId) {
        return this.serverStatus.getOrDefault(serverId, false);
    }

    public int getServerPlayerCount(String serverId) {
        String serverName = this.plugin.getConfig().getString("servers." + serverId + ".server-name");
        if (serverName == null) {
            return -1;
        }
        return this.serverPlayerCounts.getOrDefault(serverName, -1);
    }

    public void requestPlayerCount(Player player, String serverId) {
        String serverName = this.plugin.getConfig().getString("servers." + serverId + ".server-name");
        if (serverName == null) {
            return;
        }
        this.playerRequests.put(serverName, player);
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("PlayerCount");
        out.writeUTF(serverName);
        player.sendPluginMessage((Plugin)this.plugin, "BungeeCord", out.toByteArray());
    }

    public void requestAllPlayerCounts(Player player) {
        if (this.updatingPlayerCounts) {
            return;
        }
        ConfigurationSection serversSection = this.plugin.getConfig().getConfigurationSection("servers");
        if (serversSection == null) {
            return;
        }
        this.updatingPlayerCounts = true;
        this.needsMenuRefresh = false;
        this.playerRequests.clear();
        for (String serverId : serversSection.getKeys(false)) {
            this.requestPlayerCount(player, serverId);
        }
        this.plugin.getServer().getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            if (this.updatingPlayerCounts) {
                this.playerRequests.clear();
                this.updatingPlayerCounts = false;
                this.needsMenuRefresh = false;
            }
        }, 600L);
    }
}


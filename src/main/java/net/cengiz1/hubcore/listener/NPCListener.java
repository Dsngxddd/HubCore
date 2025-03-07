package net.cengiz1.hubcore.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.cengiz1.hubcore.HubCore;
import net.cengiz1.hubcore.npc.NPCManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;

public class NPCListener implements Listener {
    private final HubCore plugin;
    private NPCManager npcManager;

    public NPCListener(HubCore hubCore) {
        this.plugin = hubCore;
        this.npcManager = hubCore.getNPCManager();

        plugin.getLogger().info("NPCListener başlatılıyor...");

        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") == null) {
            hubCore.getLogger().severe("ProtocolLib eklentisi bulunamadı! NPC sistemi çalışmayacak.");
            return;
        }

        try {
            ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
            if (protocolManager == null) {
                hubCore.getLogger().severe("ProtocolManager alınamadı! NPC sistemi çalışmayacak.");
                return;
            }

            plugin.getLogger().info("PacketListener kaydediliyor...");

            // Packet listener'ı kaydet
            protocolManager.addPacketListener(
                    new PacketAdapter((Plugin)hubCore, ListenerPriority.NORMAL, PacketType.Play.Client.USE_ENTITY) {
                        @Override
                        public void onPacketReceiving(PacketEvent event) {
                            PacketContainer packet = event.getPacket();
                            Player player = event.getPlayer();

                            int entityId = packet.getIntegers().read(0);
                            EnumWrappers.EntityUseAction action = packet.getEntityUseActions().read(0);

                            plugin.getLogger().info("Entity kullanım paketi alındı: " + entityId + ", Action: " + action);

                            if (action == EnumWrappers.EntityUseAction.INTERACT ||
                                    action == EnumWrappers.EntityUseAction.INTERACT_AT) {
                                // Schedule a task to run in the main thread
                                Bukkit.getScheduler().runTask(hubCore, () -> {
                                    if (npcManager != null) {
                                        plugin.getLogger().info("NPC etkileşimi işleniyor...");
                                        npcManager.handleNPCInteract(player, entityId);
                                    } else {
                                        plugin.getLogger().warning("NPCManager null, etkileşim işlenemiyor!");
                                    }
                                });
                            }
                        }
                    }
            );

            hubCore.getLogger().info("NPC etkileşim sistemi başarıyla yüklendi.");
        } catch (Exception e) {
            hubCore.getLogger().severe("Packet listener kaydedilirken hata: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getLogger().info("Oyuncu katıldı, NPC'ler gösteriliyor: " + event.getPlayer().getName());

        // Schedule the NPC spawn a bit later to make sure the player is fully loaded
        if (npcManager != null) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                try {
                    npcManager.updateNPCsForPlayer(event.getPlayer());
                    plugin.getLogger().info("NPC'ler gösterildi: " + event.getPlayer().getName());
                } catch (Exception e) {
                    plugin.getLogger().severe("Oyuncu katıldıktan sonra NPC'ler güncellenirken hata: " + e.getMessage());
                    e.printStackTrace();
                }
            }, 20L); // 1 second delay
        } else {
            plugin.getLogger().warning("NPCManager null, oyuncu katıldığında NPC'ler güncellenemiyor.");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Clean up when player leaves
        if (npcManager != null) {
            Player player = event.getPlayer();
            plugin.getLogger().info("Oyuncu ayrıldı, NPC görünürlüğü temizleniyor: " + player.getName());
            npcManager.removePlayerVisibility(player);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        // Update NPCs when player respawns
        if (npcManager != null) {
            plugin.getLogger().info("Oyuncu yeniden doğdu, NPC'ler güncelleniyor: " + event.getPlayer().getName());

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                try {
                    npcManager.updateNPCsForPlayer(event.getPlayer());
                } catch (Exception e) {
                    plugin.getLogger().severe("Oyuncu yeniden doğduktan sonra NPC'ler güncellenirken hata: " + e.getMessage());
                    e.printStackTrace();
                }
            }, 10L);
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        // Update NPCs when player teleports to another world
        if (npcManager != null && event.getTo() != null && !event.getFrom().getWorld().equals(event.getTo().getWorld())) {
            plugin.getLogger().info("Oyuncu başka bir dünyaya ışınlandı, NPC'ler güncelleniyor: " + event.getPlayer().getName());

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                try {
                    npcManager.updateNPCsForPlayer(event.getPlayer());
                } catch (Exception e) {
                    plugin.getLogger().severe("Oyuncu ışınlandıktan sonra NPC'ler güncellenirken hata: " + e.getMessage());
                    e.printStackTrace();
                }
            }, 10L);
        }
    }
}
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

        plugin.getLogger().info("Starting NPCListener...");

        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") == null) {
            hubCore.getLogger().severe("ProtocolLib plugin not found! NPC system will not work.");
            return;
        }

        try {
            ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
            if (protocolManager == null) {
                hubCore.getLogger().severe("Unable to get ProtocolManager! NPC system will not work.");
                return;
            }

            plugin.getLogger().info("Registering PacketListener...");

            // Register packet listener
            protocolManager.addPacketListener(
                    new PacketAdapter((Plugin)hubCore, ListenerPriority.NORMAL, PacketType.Play.Client.USE_ENTITY) {
                        @Override
                        public void onPacketReceiving(PacketEvent event) {
                            PacketContainer packet = event.getPacket();
                            Player player = event.getPlayer();

                            int entityId = packet.getIntegers().read(0);
                            EnumWrappers.EntityUseAction action = packet.getEntityUseActions().read(0);

                            plugin.getLogger().info("Entity use packet received: " + entityId + ", Action: " + action);

                            if (action == EnumWrappers.EntityUseAction.INTERACT ||
                                    action == EnumWrappers.EntityUseAction.INTERACT_AT) {
                                // Schedule a task to run in the main thread
                                Bukkit.getScheduler().runTask(hubCore, () -> {
                                    if (npcManager != null) {
                                        plugin.getLogger().info("Processing NPC interaction...");
                                        npcManager.handleNPCInteract(player, entityId);
                                    } else {
                                        plugin.getLogger().warning("NPCManager is null, interaction cannot be processed!");
                                    }
                                });
                            }
                        }
                    }
            );

            hubCore.getLogger().info("NPC interaction system successfully loaded.");
        } catch (Exception e) {
            hubCore.getLogger().severe("Error registering packet listener: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getLogger().info("Player joined, showing NPCs: " + event.getPlayer().getName());

        // Schedule the NPC spawn a bit later to make sure the player is fully loaded
        if (npcManager != null) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                try {
                    npcManager.updateNPCsForPlayer(event.getPlayer());
                    plugin.getLogger().info("NPCs displayed: " + event.getPlayer().getName());
                } catch (Exception e) {
                    plugin.getLogger().severe("Error updating NPCs after player joined: " + e.getMessage());
                    e.printStackTrace();
                }
            }, 20L); // 1 second delay
        } else {
            plugin.getLogger().warning("NPCManager is null, cannot update NPCs when player joins.");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Clean up when player leaves
        if (npcManager != null) {
            Player player = event.getPlayer();
            plugin.getLogger().info("Player left, cleaning NPC visibility: " + player.getName());
            npcManager.removePlayerVisibility(player);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        // Update NPCs when player respawns
        if (npcManager != null) {
            plugin.getLogger().info("Player respawned, updating NPCs: " + event.getPlayer().getName());

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                try {
                    npcManager.updateNPCsForPlayer(event.getPlayer());
                } catch (Exception e) {
                    plugin.getLogger().severe("Error updating NPCs after player respawn: " + e.getMessage());
                    e.printStackTrace();
                }
            }, 10L);
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        // Update NPCs when player teleports to another world
        if (npcManager != null && event.getTo() != null && !event.getFrom().getWorld().equals(event.getTo().getWorld())) {
            plugin.getLogger().info("Player teleported to another world, updating NPCs: " + event.getPlayer().getName());

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                try {
                    npcManager.updateNPCsForPlayer(event.getPlayer());
                } catch (Exception e) {
                    plugin.getLogger().severe("Error updating NPCs after player teleport: " + e.getMessage());
                    e.printStackTrace();
                }
            }, 10L);
        }
    }
}
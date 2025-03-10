package net.cengiz1.hubcore.npc;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.*;
import net.cengiz1.hubcore.HubCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NPCManager {
    private final HubCore plugin;
    private ProtocolManager protocolManager;
    private static Map<String, HubNPC> npcs;
    // Changed to protected for access from NPCListener
    protected Map<UUID, Set<Integer>> visibleNpcs;
    private BukkitTask updateTask;
    // Changed to public static for access from NPCCommand
    public static final double VIEW_RANGE = 48.0;

    public NPCManager(HubCore plugin) {
        this.plugin = plugin;

        // ProtocolLib check
        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") == null) {
            return;
        }

        try {
            this.protocolManager = ProtocolLibrary.getProtocolManager();
            if (this.protocolManager == null) {
                return;
            }

            // Other initialization code
            this.npcs = new HashMap<>();
            this.visibleNpcs = new ConcurrentHashMap<>();

            loadNPCs();
            startUpdateTask();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadNPCs() {
        ConfigurationSection npcSection = plugin.getConfig().getConfigurationSection("npcs");
        if (npcSection == null) {
            // Create npcs section if it doesn't exist
            plugin.getConfig().createSection("npcs");
            plugin.saveConfig();
            return;
        }

        for (String npcId : npcSection.getKeys(false)) {
            try {
                ConfigurationSection data = npcSection.getConfigurationSection(npcId);
                if (data == null) continue;

                String worldName = data.getString("world");
                double x = data.getDouble("x");
                double y = data.getDouble("y");
                double z = data.getDouble("z");
                float yaw = (float) data.getDouble("yaw");
                float pitch = (float) data.getDouble("pitch");

                if (worldName == null || Bukkit.getWorld(worldName) == null) {
                    continue;
                }

                Location location = new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
                String name = ChatColor.translateAlternateColorCodes('&', data.getString("name", "NPC"));
                String skin = data.getString("skin", "");
                String command = data.getString("command", "");

                HubNPC npc = new HubNPC(npcId, name, location, skin, command);
                npc.setSkinName(data.getString("skin-name", ""));
                npc.setSkinSignature(data.getString("skin-signature", ""));
                npc.setLookAtPlayer(data.getBoolean("look-at-player", true));
                npc.setHologram(data.getBoolean("hologram", false));

                if (npc.isHologram()) {
                    List<String> hologramLines = data.getStringList("hologram-lines");
                    npc.setHologramLines(hologramLines);
                }

                npcs.put(npcId, npc);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void saveNPC(HubNPC npc) {
        String npcId = npc.getId();
        Location loc = npc.getLocation();

        plugin.getConfig().set("npcs." + npcId + ".world", loc.getWorld().getName());
        plugin.getConfig().set("npcs." + npcId + ".x", loc.getX());
        plugin.getConfig().set("npcs." + npcId + ".y", loc.getY());
        plugin.getConfig().set("npcs." + npcId + ".z", loc.getZ());
        plugin.getConfig().set("npcs." + npcId + ".yaw", loc.getYaw());
        plugin.getConfig().set("npcs." + npcId + ".pitch", loc.getPitch());
        plugin.getConfig().set("npcs." + npcId + ".name", npc.getName().replace('§', '&'));
        plugin.getConfig().set("npcs." + npcId + ".skin", npc.getSkin());
        plugin.getConfig().set("npcs." + npcId + ".skin-name", npc.getSkinName());
        plugin.getConfig().set("npcs." + npcId + ".skin-signature", npc.getSkinSignature());
        plugin.getConfig().set("npcs." + npcId + ".command", npc.getCommand());
        plugin.getConfig().set("npcs." + npcId + ".look-at-player", npc.isLookAtPlayer());
        plugin.getConfig().set("npcs." + npcId + ".hologram", npc.isHologram());

        if (npc.isHologram()) {
            plugin.getConfig().set("npcs." + npcId + ".hologram-lines", npc.getHologramLines());
        }

        plugin.saveConfig();
        npcs.put(npcId, npc);
    }

    public void removeNPC(String npcId) {
        HubNPC npc = npcs.remove(npcId);
        if (npc != null) {
            plugin.getConfig().set("npcs." + npcId, null);
            plugin.saveConfig();

            // Remove the NPC from all players' view
            for (UUID uuid : visibleNpcs.keySet()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null && player.isOnline()) {
                    despawnNPC(player, npc);
                }
            }
        }
    }

    public HubNPC getNPC(String npcId) {
        return npcs.get(npcId);
    }

    public Collection<HubNPC> getAllNPCs() {
        return npcs.values();
    }

    public void spawnNPC(Player player, HubNPC npc) {
        if (!player.isOnline() || !npc.getLocation().getWorld().equals(player.getWorld())) {
            return;
        }

        if (!visibleNpcs.containsKey(player.getUniqueId())) {
            visibleNpcs.put(player.getUniqueId(), new HashSet<>());
        }

        int entityId = npc.getEntityId();
        UUID npcUUID = npc.getUuid();

        try {
            // Create a new player
            PacketContainer spawnPacket = protocolManager.createPacket(PacketType.Play.Server.PLAYER_INFO);
            spawnPacket.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.ADD_PLAYER);

            WrappedGameProfile profile = new WrappedGameProfile(npcUUID, npc.getName());

            // Add skin if available
            if (!npc.getSkinName().isEmpty() && !npc.getSkinSignature().isEmpty()) {
                profile.getProperties().put("textures",
                        new WrappedSignedProperty("textures", npc.getSkinName(), npc.getSkinSignature()));
            }

            PlayerInfoData playerInfoData = new PlayerInfoData(
                    profile, 0, EnumWrappers.NativeGameMode.CREATIVE, WrappedChatComponent.fromText(npc.getName())
            );

            List<PlayerInfoData> playerInfoDataList = new ArrayList<>();
            playerInfoDataList.add(playerInfoData);
            spawnPacket.getPlayerInfoDataLists().write(0, playerInfoDataList);

            // Spawn entity
            PacketContainer entityPacket = protocolManager.createPacket(PacketType.Play.Server.NAMED_ENTITY_SPAWN);
            entityPacket.getIntegers().write(0, entityId);
            entityPacket.getUUIDs().write(0, npcUUID);
            entityPacket.getDoubles()
                    .write(0, npc.getLocation().getX())
                    .write(1, npc.getLocation().getY())
                    .write(2, npc.getLocation().getZ());
            entityPacket.getBytes()
                    .write(0, (byte) (npc.getLocation().getYaw() * 256 / 360))
                    .write(1, (byte) (npc.getLocation().getPitch() * 256 / 360));

            try {
                protocolManager.sendServerPacket(player, spawnPacket);
                protocolManager.sendServerPacket(player, entityPacket);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            // Delay the removal from the tab list
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    try {
                        PacketContainer removeInfo = protocolManager.createPacket(PacketType.Play.Server.PLAYER_INFO);
                        removeInfo.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.REMOVE_PLAYER);
                        removeInfo.getPlayerInfoDataLists().write(0, playerInfoDataList);
                        protocolManager.sendServerPacket(player, removeInfo);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 20L); // 1 second delay

            // Add NPC to visible list
            visibleNpcs.get(player.getUniqueId()).add(entityId);

            // Spawn hologram if needed
            if (npc.isHologram() && !npc.getHologramLines().isEmpty()) {
                spawnHologram(player, npc);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void despawnNPC(Player player, HubNPC npc) {
        if (!player.isOnline()) {
            return;
        }

        try {
            // Remove entity
            PacketContainer destroyPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
            destroyPacket.getIntLists().write(0, Collections.singletonList(npc.getEntityId()));

            protocolManager.sendServerPacket(player, destroyPacket);

            // Remove from visible list
            if (visibleNpcs.containsKey(player.getUniqueId())) {
                visibleNpcs.get(player.getUniqueId()).remove(npc.getEntityId());
            }

            // Despawn hologram if needed
            if (npc.isHologram()) {
                despawnHologram(player, npc);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void spawnHologram(Player player, HubNPC npc) {
        try {
            // Implementation depends on how you want to create holograms
            // This is a basic implementation using armor stands

            Location loc = npc.getLocation().clone();
            List<String> lines = npc.getHologramLines();
            int lineOffset = lines.size();

            for (String line : lines) {
                // Move up for each line
                loc = loc.clone().add(0, 0.25 + (lineOffset * 0.25), 0);
                lineOffset--;

                // Create armor stand packet
                int entityId = npc.generateHologramId();
                PacketContainer armorStandPacket = protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY);
                armorStandPacket.getIntegers().write(0, entityId);
                armorStandPacket.getUUIDs().write(0, UUID.randomUUID());
                armorStandPacket.getEntityTypeModifier().write(0, EntityType.ARMOR_STAND);
                armorStandPacket.getDoubles()
                        .write(0, loc.getX())
                        .write(1, loc.getY())
                        .write(2, loc.getZ());

                // Metadata packet
                PacketContainer metadataPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
                metadataPacket.getIntegers().write(0, entityId);

                WrappedDataWatcher dataWatcher = new WrappedDataWatcher();

                // Make armor stand invisible, no gravity
                dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(
                        0, WrappedDataWatcher.Registry.get(Byte.class)), (byte) 0x20); // Invisible
                dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(
                        5, WrappedDataWatcher.Registry.get(Boolean.class)), true); // No gravity

                // Set custom name
                dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(
                                2, WrappedDataWatcher.Registry.getChatComponentSerializer(true)),
                        Optional.of(WrappedChatComponent.fromText(ChatColor.translateAlternateColorCodes('&', line)).getHandle()));

                // Make name visible
                dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(
                        3, WrappedDataWatcher.Registry.get(Boolean.class)), true);

                // Set armor stand properties (small, no base plate, marker)
                dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(
                        14, WrappedDataWatcher.Registry.get(Byte.class)), (byte) (0x01 | 0x08 | 0x10));

                metadataPacket.getWatchableCollectionModifier().write(0, dataWatcher.getWatchableObjects());

                protocolManager.sendServerPacket(player, armorStandPacket);
                protocolManager.sendServerPacket(player, metadataPacket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void despawnHologram(Player player, HubNPC npc) {
        if (npc.getHologramIds().isEmpty()) {
            return;
        }

        try {
            PacketContainer destroyPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
            destroyPacket.getIntLists().write(0, npc.getHologramIds());

            protocolManager.sendServerPacket(player, destroyPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startUpdateTask() {
        if (protocolManager == null) {
            return;
        }

        if (updateTask != null) {
            updateTask.cancel();
        }

        updateTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateNPCsForPlayer(player);
            }
        }, 20L, 20L);
    }

    public void updateNPCsForPlayer(Player player) {
        if (protocolManager == null) {
            return;
        }

        UUID uuid = player.getUniqueId();

        if (!visibleNpcs.containsKey(uuid)) {
            visibleNpcs.put(uuid, new HashSet<>());
        }

        // Check which NPCs should be visible
        for (HubNPC npc : npcs.values()) {
            if (npc.getLocation().getWorld().equals(player.getWorld())) {
                try {
                    double distance = npc.getLocation().distance(player.getLocation());
                    boolean isVisible = visibleNpcs.get(uuid).contains(npc.getEntityId());

                    if (distance <= VIEW_RANGE && !isVisible) {
                        // Spawn NPC if in range and not visible
                        spawnNPC(player, npc);
                    } else if (distance > VIEW_RANGE && isVisible) {
                        // Despawn NPC if out of range and visible
                        despawnNPC(player, npc);
                    } else if (isVisible && npc.isLookAtPlayer()) {
                        // Update head rotation if needed
                        updateNPCRotation(player, npc);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void updateNPCRotation(Player player, HubNPC npc) {
        if (!npc.isLookAtPlayer() || !player.getWorld().equals(npc.getLocation().getWorld())) {
            return;
        }

        try {
            // Calculate the direction vector
            Location playerLoc = player.getLocation();
            Location npcLoc = npc.getLocation();

            double dx = playerLoc.getX() - npcLoc.getX();
            double dz = playerLoc.getZ() - npcLoc.getZ();

            // Calculate the yaw angle in degrees
            double yaw = Math.atan2(dz, dx);
            yaw = yaw * (180 / Math.PI);
            yaw = (yaw + 90) % 360;
            if (yaw < 0) yaw += 360;

            // Send rotation packet
            PacketContainer rotationPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_HEAD_ROTATION);
            rotationPacket.getIntegers().write(0, npc.getEntityId());
            rotationPacket.getBytes().write(0, (byte) (yaw * 256 / 360));

            // Send look packet
            PacketContainer lookPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_LOOK);
            lookPacket.getIntegers().write(0, npc.getEntityId());
            lookPacket.getBytes()
                    .write(0, (byte) (yaw * 256 / 360))
                    .write(1, (byte) 0); // Pitch
            lookPacket.getBooleans().write(0, true); // On ground

            protocolManager.sendServerPacket(player, rotationPacket);
            protocolManager.sendServerPacket(player, lookPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleNPCInteract(Player player, int entityId) {
        for (HubNPC npc : npcs.values()) {
            if (npc.getEntityId() == entityId) {
                String command = npc.getCommand();
                if (!command.isEmpty()) {
                    if (command.startsWith("/")) {
                        command = command.substring(1);
                    }

                    // Check for special handling
                    if (command.startsWith("console:")) {
                        String consoleCommand = command.substring(8);
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), consoleCommand.replace("%player%", player.getName()));
                    } else if (command.startsWith("message:")) {
                        String message = command.substring(8);
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                    } else {
                        player.chat("/" + command);
                    }
                }
                return;
            }
        }
    }

    /**
     * Checks if the NPC system is working correctly
     * @return Whether the NPC system is active
     */
    public boolean isEnabled() {
        return this.protocolManager != null;
    }

    public void shutdown() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }

        // Despawn all NPCs for all players
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (HubNPC npc : npcs.values()) {
                despawnNPC(player, npc);
            }
        }

        npcs.clear();
        visibleNpcs.clear();
    }

    public void reload() {
        shutdown();
        loadNPCs();
        startUpdateTask();

        // Spawn NPCs for all online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateNPCsForPlayer(player);
        }
    }

    /**
     * Removes a player from the visibility tracking
     * @param player The player to remove
     */
    public void removePlayerVisibility(Player player) {
        if (player != null) {
            visibleNpcs.remove(player.getUniqueId());
        }
    }
}
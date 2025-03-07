package net.cengiz1.hubcore.npc;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class HubNPC {
    private final String id;
    private final String name;
    private Location location;
    private String skin;
    private String skinName;
    private String skinSignature;
    private String command;
    private boolean lookAtPlayer;
    private boolean hologram;
    private List<String> hologramLines;
    private final List<Integer> hologramIds;

    // Each NPC gets a random entity ID
    private final int entityId;
    private final UUID uuid;

    public HubNPC(String id, String name, Location location, String skin, String command) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.skin = skin;
        this.command = command;
        this.lookAtPlayer = true;
        this.hologram = false;
        this.hologramLines = new ArrayList<>();
        this.hologramIds = new ArrayList<>();

        // Generate a random entity ID (make sure it's large to avoid conflicts)
        this.entityId = ThreadLocalRandom.current().nextInt(1000000, 2000000);
        this.uuid = UUID.randomUUID();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getSkin() {
        return skin;
    }

    public void setSkin(String skin) {
        this.skin = skin;
    }

    public String getSkinName() {
        return skinName;
    }

    public void setSkinName(String skinName) {
        this.skinName = skinName;
    }

    public String getSkinSignature() {
        return skinSignature;
    }

    public void setSkinSignature(String skinSignature) {
        this.skinSignature = skinSignature;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public boolean isLookAtPlayer() {
        return lookAtPlayer;
    }

    public void setLookAtPlayer(boolean lookAtPlayer) {
        this.lookAtPlayer = lookAtPlayer;
    }

    public boolean isHologram() {
        return hologram;
    }

    public void setHologram(boolean hologram) {
        this.hologram = hologram;
    }

    public List<String> getHologramLines() {
        return hologramLines;
    }

    public void setHologramLines(List<String> hologramLines) {
        this.hologramLines = hologramLines;
    }

    public int getEntityId() {
        return entityId;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int generateHologramId() {
        int id = ThreadLocalRandom.current().nextInt(2000000, 3000000);
        hologramIds.add(id);
        return id;
    }

    public List<Integer> getHologramIds() {
        return hologramIds;
    }
}
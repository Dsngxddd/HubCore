package net.cengiz1.multihubcore.manager;

import net.cengiz1.multihubcore.MultiHubCore;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HologramManager {
    private final MultiHubCore plugin;
    private final Map<String, List<ArmorStand>> holograms;

    public HologramManager(MultiHubCore plugin) {
        this.plugin = plugin;
        this.holograms = new HashMap<>();
        loadHolograms();
    }
    private void loadHolograms() {
        if (plugin.getConfig().getConfigurationSection("holograms") == null) return;

        for (String holoId : plugin.getConfig().getConfigurationSection("holograms").getKeys(false)) {
            Location loc = plugin.getConfig().getLocation("holograms." + holoId + ".location");
            if (loc == null) {
                plugin.getLogger().warning("Hologram location is null for ID: " + holoId);
                continue;
            }
            List<String> lines = plugin.getConfig().getStringList("holograms." + holoId + ".lines");
            createHologram(holoId, loc, lines);
        }
    }

    public void createHologram(String id, Location location, List<String> lines) {
        List<ArmorStand> stands = new ArrayList<>();
        double height = 0;

        for (String line : lines) {
            Location lineLoc = location.clone().add(0, height, 0);
            ArmorStand stand = (ArmorStand) location.getWorld().spawnEntity(lineLoc, EntityType.ARMOR_STAND);

            stand.setVisible(false);
            stand.setGravity(false);
            stand.setCustomNameVisible(true);
            stand.setCustomName(line.replace("&", "§"));

            stands.add(stand);
            height -= 0.25;
        }

        holograms.put(id, stands);
    }

    public void removeHologram(String id) {
        List<ArmorStand> stands = holograms.remove(id);
        if (stands != null) {
            stands.forEach(ArmorStand::remove);
        }
    }

    public void updateHologram(String id, List<String> newLines) {
        removeHologram(id);
        Location loc = plugin.getConfig().getLocation("holograms." + id + ".location");
        createHologram(id, loc, newLines);
    }

    public void clearAll() {
        holograms.values().forEach(stands -> stands.forEach(ArmorStand::remove));
        holograms.clear();
    }
}

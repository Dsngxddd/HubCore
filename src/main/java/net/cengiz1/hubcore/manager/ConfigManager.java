package net.cengiz1.hubcore.manager;

import net.cengiz1.hubcore.HubCore;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;

public class ConfigManager {
    private final HubCore plugin;
    private File configFile;
    private FileConfiguration config;

    public ConfigManager(HubCore plugin) {
        this.plugin = plugin;
        setupConfig();
    }

    private void setupConfig() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }

        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);
        loadDefaults();
    }

    private void loadDefaults() {
        config.options().copyDefaults(true);
        saveConfig();
    }

    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Config dosyası kaydedilemedi: " + e.getMessage());
        }
    }
}

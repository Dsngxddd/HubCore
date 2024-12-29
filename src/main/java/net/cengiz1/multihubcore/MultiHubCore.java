package net.cengiz1.multihubcore;

import net.cengiz1.multihubcore.commands.HubCommand;
import net.cengiz1.multihubcore.commands.LobbyCommand;
import net.cengiz1.multihubcore.commands.SetLobbyCommand;
import net.cengiz1.multihubcore.commands.SpawnCommand;
import net.cengiz1.multihubcore.listener.*;
import net.cengiz1.multihubcore.manager.*;
import org.bukkit.plugin.java.JavaPlugin;


public class MultiHubCore extends JavaPlugin {
    private static MultiHubCore instance;
    private ConfigManager configManager;
    private LobbyManager lobbyManager;
    private ServerManager serverManager;
    private ScoreboardManager scoreboardManager;
    private HologramManager hologramManager;
    private ItemManager itemManager;
    private MenuManager menuManager;
    private ParticleManager particleManager;

    @Override
    public void onEnable() {
        instance = this;
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.configManager = new ConfigManager(this);
        this.lobbyManager = new LobbyManager(this);
        this.serverManager = new ServerManager(this);
        this.scoreboardManager = new ScoreboardManager(this);
        this.hologramManager = new HologramManager(this);
        this.itemManager = new ItemManager(this);
        this.menuManager = new MenuManager(this);
        hologramManager = new HologramManager(this);
        this.particleManager = new ParticleManager(this);
        registerListeners();
        registerCommands();
        if (getConfig().getBoolean("mysql.enabled")) {
            setupDatabase();
        }
        getLogger().info("IumMultiHubCore başarıyla aktif edildi!");
    }

    @Override
    public void onDisable() {
        if (getConfig().getBoolean("mysql.enabled")) {
            DatabaseManager.getInstance().disconnect();
        }
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        getLogger().info("IumMultiHubCore devre dışı bırakıldı!");
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new LobbyListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new ServerSelectorListener(this), this);
        getServer().getPluginManager().registerEvents(new ProtectionListener(this), this);
    }

    private void registerCommands() {
        getCommand("lobby").setExecutor(new LobbyCommand(this));
        getCommand("setlobby").setExecutor(new SetLobbyCommand(this));
        getCommand("hub").setExecutor(new HubCommand(this));
        getCommand("spawn").setExecutor(new SpawnCommand(this));
    }

    private void setupDatabase() {
        String host = getConfig().getString("mysql.host");
        String database = getConfig().getString("mysql.database");
        String username = getConfig().getString("mysql.username");
        String password = getConfig().getString("mysql.password");
        int port = getConfig().getInt("mysql.port");
        String prefix = getConfig().getString("mysql.table-prefix");

        DatabaseManager.initialize(host, port, database, username, password, prefix);
    }

    public static MultiHubCore getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() { return configManager; }
    public LobbyManager getLobbyManager() { return lobbyManager; }
    public ServerManager getServerManager() { return serverManager; }
    public ScoreboardManager getScoreboardManager() { return scoreboardManager; }
    public HologramManager getHologramManager() { return hologramManager; }
    public ItemManager getItemManager() { return itemManager; }
    public MenuManager getMenuManager() { return menuManager; }
    public ParticleManager getParticleManager() { return particleManager; }
}
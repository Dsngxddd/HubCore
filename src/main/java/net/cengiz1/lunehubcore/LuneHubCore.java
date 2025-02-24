package net.cengiz1.lunehubcore;

import net.cengiz1.lunehubcore.commands.*;
import net.cengiz1.lunehubcore.listener.*;
import net.cengiz1.lunehubcore.manager.*;
import net.cengiz1.lunehubcore.util.CommandHelper;
import org.bukkit.plugin.java.JavaPlugin;

public class LuneHubCore extends JavaPlugin {
    private static LuneHubCore instance;
    private ConfigManager configManager;
    private LobbyManager lobbyManager;
    private ServerManager serverManager;
    private ScoreboardManager scoreboardManager;
    private ItemManager itemManager;
    private MenuManager menuManager;
    private ParticleManager particleManager;
    private PlayerHiderManager playerHiderManager;
    private ChatListener chatListener;
    private CommandHelper commandHelper;

    @Override
    public void onEnable() {
        instance = this;
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", (channel, player, message) -> {
        });
        this.commandHelper = new CommandHelper(this);
        this.configManager = new ConfigManager(this);
        this.lobbyManager = new LobbyManager(this);
        this.serverManager = new ServerManager(this);
        this.scoreboardManager = new ScoreboardManager(this);
        this.itemManager = new ItemManager(this);
        this.menuManager = new MenuManager(this);
        this.playerHiderManager = new PlayerHiderManager(this);
        this.chatListener = new ChatListener(this);

        this.particleManager = new ParticleManager(this);
        registerListeners();
        registerCommands();
        if (getConfig().getBoolean("mysql.enabled")) {
            setupDatabase();
        }
        getLogger().setLevel(java.util.logging.Level.INFO);
        getLogger().info("LuneHubCore başarıyla aktif edildi!");
    }

    @Override
    public void onDisable() {
        if (getConfig().getBoolean("mysql.enabled")) {
            DatabaseManager.getInstance().disconnect();
        }
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        this.getServer().getMessenger().unregisterIncomingPluginChannel(this, "BungeeCord");
        getLogger().info("LuneHubCore devre dışı bırakıldı!");
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new LobbyListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new ServerSelectorListener(this), this);
        getServer().getPluginManager().registerEvents(new ProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(chatListener, this);
    }

    private void registerCommands() {
        getCommand("lobby").setExecutor(new LobbyCommand(this));
        getCommand("setlobby").setExecutor(new SetLobbyCommand(this));
        getCommand("hub").setExecutor(new HubCommand(this));
        getCommand("spawn").setExecutor(new SpawnCommand(this));
        getCommand("multihubreload").setExecutor(new ReloadCommand(this));
        getCommand("chatlock").setExecutor(new ChatLockCommand(this));
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

    public static LuneHubCore getInstance() {
        return instance;
    }

    public CommandHelper getCommandHelper() {
        return commandHelper;
    }

    public ChatListener getChatListener() {
        return chatListener;
    }

    public PlayerHiderManager getPlayerHiderManager() { return playerHiderManager; }
    public ConfigManager getConfigManager() { return configManager; }
    public LobbyManager getLobbyManager() { return lobbyManager; }
    public ServerManager getServerManager() { return serverManager; }
    public ScoreboardManager getScoreboardManager() { return scoreboardManager; }
    public ItemManager getItemManager() { return itemManager; }
    public MenuManager getMenuManager() { return menuManager; }
    public ParticleManager getParticleManager() { return particleManager; }
}
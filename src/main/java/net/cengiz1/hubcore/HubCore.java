package net.cengiz1.hubcore;

import net.cengiz1.hubcore.commands.*;
import net.cengiz1.hubcore.listener.*;
import net.cengiz1.hubcore.manager.*;
import net.cengiz1.hubcore.npc.NPCManager;
import net.cengiz1.hubcore.util.CommandHelper;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class HubCore extends JavaPlugin {
    private static HubCore instance;
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
    private VIPManager vipManager;
    private AnnouncementManager announcementManager;
    private NPCManager npcManager;
    private NPCListener npcListener;

    @Override
    public void onEnable() {
        instance = this;
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", (channel, player, message) -> {});
        this.commandHelper = new CommandHelper(this);
        this.configManager = new ConfigManager(this);
        this.lobbyManager = new LobbyManager(this);
        this.serverManager = new ServerManager(this);
        this.scoreboardManager = new ScoreboardManager(this);
        this.itemManager = new ItemManager(this);
        this.menuManager = new MenuManager(this);
        this.playerHiderManager = new PlayerHiderManager(this);
        this.chatListener = new ChatListener(this);
        this.vipManager = new VIPManager(this);
        this.particleManager = new ParticleManager(this);
        registerCommands();
        registerBasicListeners();
        if (getConfig().getBoolean("mysql.enabled")) {
            setupDatabase();
        }
        this.announcementManager = new AnnouncementManager(this);

        setupNPCSystem();
    }

    @Override
    public void onDisable() {
        if (getConfig().getBoolean("mysql.enabled")) {
            DatabaseManager.getInstance().disconnect();
        }
        if (announcementManager != null) {
            announcementManager.stopAnnouncementTask();
        }
        if (npcManager != null) {
            npcManager.shutdown();
        }

        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        this.getServer().getMessenger().unregisterIncomingPluginChannel(this, "BungeeCord");
    }

    private void registerBasicListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new LobbyListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new ServerSelectorListener(this), this);
        getServer().getPluginManager().registerEvents(new ProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(chatListener, this);
    }
    private void setupNPCSystem() {
        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
            try {
                this.npcManager = new NPCManager(this);
                if (this.npcManager != null && this.npcManager.isEnabled()) {
                    this.npcListener = new NPCListener(this);
                    if (this.npcListener != null) {
                        getServer().getPluginManager().registerEvents(this.npcListener, this);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void registerCommands() {
        getCommand("lobby").setExecutor(new LobbyCommand(this));
        getCommand("setlobby").setExecutor(new SetLobbyCommand(this));
        getCommand("hub").setExecutor(new HubCommand(this));
        getCommand("spawn").setExecutor(new SpawnCommand(this));
        getCommand("multihubreload").setExecutor(new ReloadCommand(this));
        getCommand("chatlock").setExecutor(new ChatLockCommand(this));
        getCommand("announce").setExecutor(new AnnounceCommand(this));
        if (getCommand("npc") != null && this.npcManager != null) {
            NPCCommand npcCommand = new NPCCommand(this);
            getCommand("npc").setExecutor(npcCommand);
            getCommand("npc").setTabCompleter(npcCommand);
        }
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

    public static HubCore getInstance() {
        return instance;
    }

    public CommandHelper getCommandHelper() { return commandHelper; }
    public ChatListener getChatListener() { return chatListener; }
    public PlayerHiderManager getPlayerHiderManager() { return playerHiderManager; }
    public ConfigManager getConfigManager() { return configManager; }
    public LobbyManager getLobbyManager() { return lobbyManager; }
    public ServerManager getServerManager() { return serverManager; }
    public ScoreboardManager getScoreboardManager() { return scoreboardManager; }
    public ItemManager getItemManager() { return itemManager; }
    public MenuManager getMenuManager() { return menuManager; }
    public ParticleManager getParticleManager() { return particleManager; }
    public VIPManager getVIPManager() { return vipManager; }
    public AnnouncementManager getAnnouncementManager() { return announcementManager; }
    public NPCManager getNPCManager() { return npcManager; }
}
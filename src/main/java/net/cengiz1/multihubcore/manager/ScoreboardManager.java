package net.cengiz1.multihubcore.manager;

import net.cengiz1.multihubcore.MultiHubCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import java.util.List;

public class ScoreboardManager {
    private final MultiHubCore plugin;

    public ScoreboardManager(MultiHubCore plugin) {
        this.plugin = plugin;
        startScoreboardUpdater();
    }

    public void updateScoreboard(Player player) {
        if (!plugin.getConfig().getBoolean("scoreboard.enabled")) return;

        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        String title = plugin.getConfig().getString("scoreboard.title").replace("&", "§");
        Objective obj = board.registerNewObjective("hubboard", "dummy", title);
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        String currentLobby = plugin.getLobbyManager().getPlayerLobby(player);
        int players = plugin.getLobbyManager().getLobbyPlayerCount(currentLobby);
        int maxPlayers = plugin.getConfig().getInt("lobbies." + currentLobby + ".max-players");

        List<String> lines = plugin.getConfig().getStringList("scoreboard.lines");
        int lineNumber = lines.size();

        for (String line : lines) {
            line = line.replace("%lobby%", currentLobby)
                    .replace("%online%", String.valueOf(players))
                    .replace("%max%", String.valueOf(maxPlayers))
                    .replace("&", "§");

            // Sunucu durumlarını ekle
            for (ServerInfo server : plugin.getServerManager().getServers().values()) {
                String status = server.isOnline() ? "§aAKTİF" : "§cKAPALI";
                line = line.replace("%" + server.getId() + "_status%", status);
            }

            Score score = obj.getScore(line);
            score.setScore(lineNumber--);
        }

        player.setScoreboard(board);
    }

    private void startScoreboardUpdater() {
        int interval = plugin.getConfig().getInt("scoreboard.update-interval");
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateScoreboard(player);
            }
        }, 20L, interval);
    }
}
package net.cengiz1.hubcore.commands;

import net.cengiz1.hubcore.HubCore;
import net.cengiz1.hubcore.npc.NPCManager;
import net.cengiz1.hubcore.npc.HubNPC;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NPCCommand implements CommandExecutor, TabCompleter {
    private final HubCore plugin;
    private final List<String> subCommands = Arrays.asList(
            "create", "remove", "list", "setskin", "setcommand", "teleport", "move", "reload", "lookatplayer", "hologram"
    );

    public NPCCommand(HubCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Bu komut sadece oyuncular tarafından kullanılabilir!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("hubcore.npc")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.no-permission", "&cBu komutu kullanma yetkiniz yok!")));
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Kullanım: /npc create <id> [isim]");
                    return true;
                }
                createNPC(player, args);
                break;
            case "remove":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Kullanım: /npc remove <id>");
                    return true;
                }
                removeNPC(player, args[1]);
                break;
            case "list":
                listNPCs(player);
                break;
            case "setskin":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Kullanım: /npc setskin <id> <skin>");
                    return true;
                }
                setSkin(player, args[1], args[2]);
                break;
            case "setcommand":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Kullanım: /npc setcommand <id> <komut>");
                    return true;
                }
                setCommand(player, args);
                break;
            case "teleport":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Kullanım: /npc teleport <id>");
                    return true;
                }
                teleportToNPC(player, args[1]);
                break;
            case "move":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Kullanım: /npc move <id>");
                    return true;
                }
                moveNPC(player, args[1]);
                break;
            case "reload":
                reloadNPCs(player);
                break;
            case "lookatplayer":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Kullanım: /npc lookatplayer <id> <true/false>");
                    return true;
                }
                setLookAtPlayer(player, args[1], Boolean.parseBoolean(args[2]));
                break;
            case "hologram":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Kullanım: /npc hologram <id> <add/remove/clear> [text]");
                    return true;
                }
                handleHologram(player, args);
                break;
            default:
                sendHelp(player);
                break;
        }

        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "----- HubCore NPC Komutları -----");
        player.sendMessage(ChatColor.YELLOW + "/npc create <id> [isim] " + ChatColor.GRAY + "- Yeni bir NPC oluşturur");
        player.sendMessage(ChatColor.YELLOW + "/npc remove <id> " + ChatColor.GRAY + "- Bir NPC'yi kaldırır");
        player.sendMessage(ChatColor.YELLOW + "/npc list " + ChatColor.GRAY + "- Tüm NPC'leri listeler");
        player.sendMessage(ChatColor.YELLOW + "/npc setskin <id> <skin> " + ChatColor.GRAY + "- NPC'nin skinini değiştirir");
        player.sendMessage(ChatColor.YELLOW + "/npc setcommand <id> <komut> " + ChatColor.GRAY + "- NPC'ye komut ekler");
        player.sendMessage(ChatColor.YELLOW + "/npc teleport <id> " + ChatColor.GRAY + "- NPC'ye ışınlanır");
        player.sendMessage(ChatColor.YELLOW + "/npc move <id> " + ChatColor.GRAY + "- NPC'yi bulunduğunuz yere taşır");
        player.sendMessage(ChatColor.YELLOW + "/npc reload " + ChatColor.GRAY + "- NPC'leri yeniden yükler");
        player.sendMessage(ChatColor.YELLOW + "/npc lookatplayer <id> <true/false> " + ChatColor.GRAY + "- NPC'nin oyuncuya bakmasını ayarlar");
        player.sendMessage(ChatColor.YELLOW + "/npc hologram <id> <add/remove/clear> [text] " + ChatColor.GRAY + "- NPC üzerinde hologram ekler/siler");
    }

    private void createNPC(Player player, String[] args) {
        String id = args[1];
        String name = args.length > 2 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : id;

        // Check if NPC already exists
        if (plugin.getNPCManager().getNPC(id) != null) {
            player.sendMessage(ChatColor.RED + "Bu ID'ye sahip bir NPC zaten var: " + id);
            return;
        }

        // Create the NPC at player's location
        HubNPC npc = new HubNPC(id, ChatColor.translateAlternateColorCodes('&', name), player.getLocation(), "", "");
        plugin.getNPCManager().saveNPC(npc);

        // Spawn the NPC for nearby players
        for (Player onlinePlayer : player.getWorld().getPlayers()) {
            if (onlinePlayer.getLocation().distance(player.getLocation()) <= NPCManager.VIEW_RANGE) {
                plugin.getNPCManager().spawnNPC(onlinePlayer, npc);
            }
        }

        player.sendMessage(ChatColor.GREEN + "NPC başarıyla oluşturuldu: " + id);
    }

    private void removeNPC(Player player, String id) {
        HubNPC npc = plugin.getNPCManager().getNPC(id);
        if (npc == null) {
            player.sendMessage(ChatColor.RED + "Bu ID'ye sahip bir NPC bulunamadı: " + id);
            return;
        }

        plugin.getNPCManager().removeNPC(id);
        player.sendMessage(ChatColor.GREEN + "NPC başarıyla kaldırıldı: " + id);
    }

    private void listNPCs(Player player) {
        Collection<HubNPC> npcs = plugin.getNPCManager().getAllNPCs();
        if (npcs.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "Hiç NPC bulunamadı.");
            return;
        }

        player.sendMessage(ChatColor.GOLD + "----- NPC Listesi (" + npcs.size() + ") -----");
        for (HubNPC npc : npcs) {
            player.sendMessage(ChatColor.YELLOW + npc.getId() + ChatColor.GRAY + " - " +
                    ChatColor.WHITE + npc.getName() + ChatColor.GRAY + " - " +
                    "Dünya: " + npc.getLocation().getWorld().getName() +
                    ", X: " + Math.round(npc.getLocation().getX()) +
                    ", Y: " + Math.round(npc.getLocation().getY()) +
                    ", Z: " + Math.round(npc.getLocation().getZ()));
        }
    }

    private void setSkin(Player player, String id, String skinName) {
        HubNPC npc = plugin.getNPCManager().getNPC(id);
        if (npc == null) {
            player.sendMessage(ChatColor.RED + "Bu ID'ye sahip bir NPC bulunamadı: " + id);
            return;
        }

        // Here you would fetch skin data from Mojang API or a skin database
        // This is a simplified version that just sets the skin name
        npc.setSkin(skinName);
        player.sendMessage(ChatColor.GREEN + "NPC skini ayarlandı. Lütfen /npc reload komutunu kullanarak değişiklikleri uygulayın.");

        // Save the NPC
        plugin.getNPCManager().saveNPC(npc);
    }

    private void setCommand(Player player, String[] args) {
        String id = args[1];
        String command = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

        HubNPC npc = plugin.getNPCManager().getNPC(id);
        if (npc == null) {
            player.sendMessage(ChatColor.RED + "Bu ID'ye sahip bir NPC bulunamadı: " + id);
            return;
        }

        npc.setCommand(command);
        plugin.getNPCManager().saveNPC(npc);

        player.sendMessage(ChatColor.GREEN + "NPC komutu ayarlandı: " + command);
    }

    private void teleportToNPC(Player player, String id) {
        HubNPC npc = plugin.getNPCManager().getNPC(id);
        if (npc == null) {
            player.sendMessage(ChatColor.RED + "Bu ID'ye sahip bir NPC bulunamadı: " + id);
            return;
        }

        player.teleport(npc.getLocation());
        player.sendMessage(ChatColor.GREEN + "NPC'ye ışınlandınız: " + id);
    }

    private void moveNPC(Player player, String id) {
        HubNPC npc = plugin.getNPCManager().getNPC(id);
        if (npc == null) {
            player.sendMessage(ChatColor.RED + "Bu ID'ye sahip bir NPC bulunamadı: " + id);
            return;
        }

        // First despawn the NPC from all players
        for (Player onlinePlayer : player.getWorld().getPlayers()) {
            plugin.getNPCManager().despawnNPC(onlinePlayer, npc);
        }

        // Update location
        npc.setLocation(player.getLocation());
        plugin.getNPCManager().saveNPC(npc);

        // Respawn the NPC for nearby players
        for (Player onlinePlayer : player.getWorld().getPlayers()) {
            if (onlinePlayer.getLocation().distance(player.getLocation()) <= NPCManager.VIEW_RANGE) {
                plugin.getNPCManager().spawnNPC(onlinePlayer, npc);
            }
        }

        player.sendMessage(ChatColor.GREEN + "NPC başarıyla taşındı: " + id);
    }

    private void reloadNPCs(Player player) {
        plugin.getNPCManager().reload();
        player.sendMessage(ChatColor.GREEN + "NPC'ler başarıyla yeniden yüklendi.");
    }

    private void setLookAtPlayer(Player player, String id, boolean lookAtPlayer) {
        HubNPC npc = plugin.getNPCManager().getNPC(id);
        if (npc == null) {
            player.sendMessage(ChatColor.RED + "Bu ID'ye sahip bir NPC bulunamadı: " + id);
            return;
        }

        npc.setLookAtPlayer(lookAtPlayer);
        plugin.getNPCManager().saveNPC(npc);

        player.sendMessage(ChatColor.GREEN + "NPC'nin oyuncuya bakması " +
                (lookAtPlayer ? "aktif edildi" : "devre dışı bırakıldı") + ": " + id);
    }

    private void handleHologram(Player player, String[] args) {
        String id = args[1];
        String action = args[2].toLowerCase();

        HubNPC npc = plugin.getNPCManager().getNPC(id);
        if (npc == null) {
            player.sendMessage(ChatColor.RED + "Bu ID'ye sahip bir NPC bulunamadı: " + id);
            return;
        }

        switch (action) {
            case "add":
                if (args.length < 4) {
                    player.sendMessage(ChatColor.RED + "Kullanım: /npc hologram " + id + " add <metin>");
                    return;
                }
                String text = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                List<String> lines = npc.getHologramLines();
                lines.add(text);
                npc.setHologram(true);
                plugin.getNPCManager().saveNPC(npc);

                // Refresh the NPC for all nearby players
                refreshNPCForPlayers(npc);

                player.sendMessage(ChatColor.GREEN + "Hologram satırı eklendi: " + text);
                break;

            case "remove":
                if (args.length < 4) {
                    player.sendMessage(ChatColor.RED + "Kullanım: /npc hologram " + id + " remove <index>");
                    return;
                }

                try {
                    int index = Integer.parseInt(args[3]) - 1; // Convert to 0-based index
                    List<String> hologramLines = npc.getHologramLines();

                    if (index < 0 || index >= hologramLines.size()) {
                        player.sendMessage(ChatColor.RED + "Geçersiz satır indeksi. Mevcut satır sayısı: " + hologramLines.size());
                        return;
                    }

                    String removedLine = hologramLines.remove(index);
                    if (hologramLines.isEmpty()) {
                        npc.setHologram(false);
                    }

                    plugin.getNPCManager().saveNPC(npc);

                    // Refresh the NPC for all nearby players
                    refreshNPCForPlayers(npc);

                    player.sendMessage(ChatColor.GREEN + "Hologram satırı silindi: " + removedLine);
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Geçersiz indeks numarası: " + args[3]);
                }
                break;

            case "clear":
                npc.getHologramLines().clear();
                npc.setHologram(false);
                plugin.getNPCManager().saveNPC(npc);

                // Refresh the NPC for all nearby players
                refreshNPCForPlayers(npc);

                player.sendMessage(ChatColor.GREEN + "Tüm hologram satırları silindi.");
                break;

            default:
                player.sendMessage(ChatColor.RED + "Geçersiz hologram komutu. Kullanım: /npc hologram <id> <add/remove/clear> [text]");
                break;
        }
    }

    private void refreshNPCForPlayers(HubNPC npc) {
        for (Player onlinePlayer : npc.getLocation().getWorld().getPlayers()) {
            if (onlinePlayer.getLocation().distance(npc.getLocation()) <= NPCManager.VIEW_RANGE) {
                plugin.getNPCManager().despawnNPC(onlinePlayer, npc);
                plugin.getNPCManager().spawnNPC(onlinePlayer, npc);
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return subCommands.stream()
                    .filter(sub -> sub.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("remove") || subCommand.equals("setskin") ||
                    subCommand.equals("setcommand") || subCommand.equals("teleport") ||
                    subCommand.equals("move") || subCommand.equals("lookatplayer") ||
                    subCommand.equals("hologram")) {

                // Return list of NPC IDs
                return plugin.getNPCManager().getAllNPCs().stream()
                        .map(HubNPC::getId)
                        .filter(id -> id.startsWith(args[1]))
                        .collect(Collectors.toList());
            }
        }

        if (args.length == 3) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("lookatplayer")) {
                return Arrays.asList("true", "false").stream()
                        .filter(bool -> bool.startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (subCommand.equals("hologram")) {
                return Arrays.asList("add", "remove", "clear").stream()
                        .filter(action -> action.startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        return new ArrayList<>();
    }
}
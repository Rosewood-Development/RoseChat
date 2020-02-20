package me.lilac.rosechat.commands;

import me.lilac.rosechat.Rosechat;
import me.lilac.rosechat.placeholder.FormatType;
import me.lilac.rosechat.placeholder.PlaceholderMessage;
import me.lilac.rosechat.storage.Messages;
import me.lilac.rosechat.storage.PlayerData;
import me.lilac.rosechat.storage.Settings;
import me.lilac.rosechat.utils.Methods;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CommandMessage implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String cmd, String[] args) {
        if (!(sender instanceof Player)) return false;
         if (sender.hasPermission("rosechat.message")) {
            if (args.length == 0) {
                sender.sendMessage(Methods.format(Messages.getChoosePlayer()));
            } else if (args.length > 0) {
                if (Bukkit.getPlayer(args[0]) != null) {
                    Player player = (Player) sender;
                    Player target = Bukkit.getPlayer(args[0]);

                    if (PlayerData.getPlayersWithoutMessages().contains(target.getUniqueId())) {
                        player.sendMessage(Methods.format(Messages.getCannotMessage()));
                        return false;
                    }

                    TextComponent forSender = new PlaceholderMessage(player, target, getMessage(args), FormatType.MSG_SENT).getMessage();
                    TextComponent forTarget = new PlaceholderMessage(player, target, getMessage(args), FormatType.MSG_RECEIVE).getMessage();

                    player.spigot().sendMessage(forSender);
                    target.spigot().sendMessage(forTarget);
                    Rosechat.getInstance().getChatManager().getLastReply().put(player.getUniqueId(), target.getUniqueId());
                    Rosechat.getInstance().getChatManager().getLastReply().put(target.getUniqueId(), player.getUniqueId());

                    if (!PlayerData.getPlayersWithoutSounds().contains(target.getUniqueId()))
                        target.playSound(target.getLocation(), Settings.getMessageSound(), 1, 1);

                    for (UUID spy : PlayerData.getPlayersUsingSocialSpy()) {
                        if (player.getUniqueId().equals(spy) || target.getUniqueId().equals(spy)) continue;
                        if (Bukkit.getPlayer(spy) == null) continue;
                        Bukkit.getPlayer(spy).sendMessage(Methods.format(Messages.getSocialSpyPrefix()
                                .replace("%sender%", player.getName())
                                .replace("%target%", target.getName())
                                .replace("%message%", getMessage(args))));
                    }
                } else {
                    sender.sendMessage(Methods.format(Messages.getPlayerOffline()));
                }
            }
        } else {
            sender.sendMessage(Methods.format(Messages.getNoPermission()));
        }
        return false;
    }

    private String getMessage(String[] args) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < args.length; i++){
            sb.append(args[i]).append(" ");
        }

        return sb.toString().trim();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String cmd, String[] args) {
        List<String> tab = new ArrayList<>();

        if (!sender.hasPermission("rosechat.message")) return tab;

        if (args.length == 1) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (args[0] != null) {
                    if (player.getName().toLowerCase().contains(args[0].toLowerCase())) {
                        tab.add(player.getName());
                    }
                } else {
                    tab.add(player.getName());
                }
            }
        }

        return tab;
    }
}

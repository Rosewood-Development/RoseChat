package me.lilac.rosechat.commands;

import me.lilac.rosechat.storage.Messages;
import me.lilac.rosechat.storage.PlayerData;
import me.lilac.rosechat.storage.Settings;
import me.lilac.rosechat.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CommandBroadcast implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String cmd, String[] args) {
        if (sender.hasPermission("rosechat.broadcast")) {
            if (args.length == 0) {
                sender.sendMessage(Methods.format(Messages.getEnterMessage()));
            } else if (args.length > 0) {
                String message = getMessage(args);
                Bukkit.broadcastMessage(Methods.format(Messages.getBroadcastPrefix().replace("%message%", message)));

                for (Player online : Bukkit.getOnlinePlayers()) {
                    if (!PlayerData.getPlayersWithoutSounds().contains(online.getUniqueId())) {
                        online.playSound(online.getLocation(), Settings.getBroadcastSound(), 1, 1);
                    }
                }
            }
        } else {
            sender.sendMessage(Methods.format(Messages.getNoPermission()));
        }

        return false;
    }

    private String getMessage(String[] args) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++){
            sb.append(args[i]).append(" ");
        }

        return sb.toString().trim();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] strings) {
        return new ArrayList<>();
    }
}

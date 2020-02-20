package me.lilac.rosechat.commands;

import me.lilac.rosechat.Rosechat;
import me.lilac.rosechat.storage.Messages;
import me.lilac.rosechat.utils.Methods;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandRosechat implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String cmd, String[] args) {
        if (sender.hasPermission("rosechat.admin")) {
            if (args.length == 0) {
                sendHelpMessage(sender);
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("reload")) {
                    Rosechat.getInstance().reload();
                    sender.sendMessage(Methods.format("&8[&cRosechat&8] &cReloaded."));
                } else {
                    sender.sendMessage(Methods.format(Messages.getInvalidArgument()));
                }
            }
        } else {
            sender.sendMessage(Methods.format("&8[&cRosechat&8] &cby &dLilac \u2764"));
        }

        return false;
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(Methods.format("&8[&cRosechat&8] &cby &dLilac \u2764"));
        sender.sendMessage(Methods.format("&c/rosechat reload &7- Reloads the config file."));
        sender.sendMessage(Methods.format("&c/msg <player> &7- Privately messages a player."));
        sender.sendMessage(Methods.format("&c/reply &7- Replies to the last private message."));
        sender.sendMessage(Methods.format("&c/broadcast <message> &7- Sends a broadcast."));
        sender.sendMessage(Methods.format("&c/staffchat [message] &7- Sends a message to the staff."));
        sender.sendMessage(Methods.format("&c/togglesound &7- Toggles sounds from a /msg or /bc."));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String cmd, String[] args) {
        if (args.length == 1) return Arrays.asList("reload");
        return new ArrayList<>();
    }
}

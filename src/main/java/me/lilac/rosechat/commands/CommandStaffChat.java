package me.lilac.rosechat.commands;

import me.lilac.rosechat.Rosechat;
import me.lilac.rosechat.placeholder.FormatType;
import me.lilac.rosechat.placeholder.PlaceholderMessage;
import me.lilac.rosechat.storage.Messages;
import me.lilac.rosechat.storage.PlayerData;
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

public class CommandStaffChat implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String cmd, String[] args) {
        if (!(sender instanceof Player)) return false;
        if (sender.hasPermission("rosechat.staffchat")) {
            if (args.length == 0) {
                toggleStaffChat((Player) sender);
            } else if (args.length > 0) {
                TextComponent finalMessage = new PlaceholderMessage((Player) sender, getMessage(args), FormatType.STAFF_CHAT).getMessage();
                for (Player online : Bukkit.getOnlinePlayers()) {
                    if (!online.hasPermission("rosechat.staffchat")) return false;
                    online.spigot().sendMessage(finalMessage);
                }
                Bukkit.getConsoleSender().sendMessage(finalMessage.toLegacyText());
                return false;
            }
        } else {
            sender.sendMessage(Methods.format(Messages.getNoPermission()));
        }

        return false;
    }

    private void toggleStaffChat(Player player) {
        if (!PlayerData.getPlayersUsingStaffchat().contains(player.getUniqueId())) {
            PlayerData.getPlayersUsingStaffchat().add(player.getUniqueId());
            player.sendMessage(Methods.format(Messages.getToggleStaffChatOn()));
        } else {
            PlayerData.getPlayersUsingStaffchat().remove(player.getUniqueId());
            player.sendMessage(Methods.format(Messages.getToggleStaffChatOff()));
        }
        Rosechat.getInstance().getConfigManager().saveData();
    }

    private String getMessage(String[] args) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++){
            sb.append(args[i]).append(" ");
        }

        return sb.toString().trim();
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return new ArrayList<>();
    }
}

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

public class CommandReply implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String cmd, String[] args) {
        if (sender instanceof Player) {
            if (sender.hasPermission("rosechat.reply")) {
                Player player = (Player) sender;

                if (args.length > 0) {
                    if (Rosechat.getInstance().getChatManager().getLastReply().containsKey(player.getUniqueId())) {
                        UUID targetUUID = Rosechat.getInstance().getChatManager().getLastReply().get(player.getUniqueId());

                        if (Bukkit.getPlayer(targetUUID) == null) return false;
                        Player target = Bukkit.getPlayer(targetUUID);

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
                        player.sendMessage(Methods.format(Messages.getNoReply()));
                    }
                } else {
                    sender.sendMessage(Methods.format(Messages.getInvalidArgument()));
                }
            } else {
                sender.sendMessage(Methods.format(Messages.getNoPermission()));
            }
        } else {
            Methods.sendConsoleMessage("&cThe console cannot use this command!");
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
    public List<String> onTabComplete(CommandSender sender, Command command, String cmd, String[] args) {
        return new ArrayList<>();
    }
}

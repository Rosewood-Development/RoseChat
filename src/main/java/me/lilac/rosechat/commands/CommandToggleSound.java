package me.lilac.rosechat.commands;

import me.lilac.rosechat.Rosechat;
import me.lilac.rosechat.storage.Messages;
import me.lilac.rosechat.storage.PlayerData;
import me.lilac.rosechat.utils.Methods;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CommandToggleSound implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String cmd, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (player.hasPermission("rosechat.togglesound")) {
                if (!PlayerData.getPlayersWithoutSounds().contains(player.getUniqueId())) {
                    PlayerData.getPlayersWithoutSounds().add(player.getUniqueId());
                    player.sendMessage(Methods.format(Messages.getToggleSoundOff()));
                } else {
                    PlayerData.getPlayersWithoutSounds().remove(player.getUniqueId());
                    player.sendMessage(Methods.format(Messages.getToggleSoundOn()));
                }

                Rosechat.getInstance().getConfigManager().saveData();
            } else {
                player.sendMessage(Methods.format(Messages.getNoPermission()));
            }
        } else {
            Methods.sendConsoleMessage("&cThe console cannot use this command!");
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return new ArrayList<>();
    }
}

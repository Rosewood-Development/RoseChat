package dev.rosewood.rosechat.command.chat;

import dev.rosewood.rosechat.chat.ChatChannel;
import dev.rosewood.rosechat.message.MessageSender;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

public class SudoChatCommand extends AbstractCommand {

    public SudoChatCommand() {
        super(true, "sudo");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length != 3) {
            this.getAPI().getLocaleManager().sendMessage(sender, "invalid-arguments", StringPlaceholders.single("syntax", this.getSyntax()));
            return;
        }

        String player = args[0];
        ChatChannel channel = this.getAPI().getChannelById(args[1]);

        MessageSender sudoSender = new MessageSender(player, "default");

        //TODO: Sudo
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();

        if (args.length == 1) {
            for (Player player : Bukkit.getOnlinePlayers()) tab.add(player.getName());
        } else if (args.length == 2) {
            tab.addAll(this.getAPI().getChannelIDs());
        }

        ///chat sudo <player> <channel> <message>

        return tab;
    }

    @Override
    public String getPermission() {
        return "rosechat.admin.sudo";
    }

    @Override
    public String getSyntax() {
        return this.getAPI().getLocaleManager().getLocaleMessage("command-chat-sudo-usage");
    }
}

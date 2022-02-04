package dev.rosewood.rosechat.command.chat;

import dev.rosewood.rosechat.chat.ChatChannel;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

public class ClearChatCommand extends AbstractCommand {

    public ClearChatCommand() {
        super(false, "clear");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        RoseSender roseSender = new RoseSender(sender);
        ChatChannel channel = null;

        if (args.length == 0) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                channel = this.getAPI().getPlayerData(player.getUniqueId()).getCurrentChannel();
            } else {
                this.getAPI().getLocaleManager().sendComponentMessage(roseSender, "invalid-arguments", StringPlaceholders.single("syntax", this.getSyntax()));
            }
        } else if (args.length == 1) {
            channel = this.getAPI().getChannelById(args[0]);
        }

        if (channel == null) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-channel-not-found");
            return;
        }

        for (int i = 0; i < 100; i++) channel.clear("\n");

        this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-chat-clear-cleared", StringPlaceholders.single("channel", channel.getId()));
        if (sender instanceof Player) {
            this.getAPI().getLocaleManager().sendComponentMessage(Bukkit.getConsoleSender(), "command-chat-clear-cleared", StringPlaceholders.single("channel", channel.getId()));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();

        if (args.length == 1) tab.addAll(this.getAPI().getChannelIDs());

        return tab;
    }

    @Override
    public String getPermission() {
        return "rosechat.admin.clear";
    }

    @Override
    public String getSyntax() {
        return this.getAPI().getLocaleManager().getLocaleMessage("command-chat-clear-usage");
    }
}

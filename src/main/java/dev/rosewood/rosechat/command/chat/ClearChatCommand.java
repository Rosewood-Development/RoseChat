package dev.rosewood.rosechat.command.chat;

import dev.rosewood.rosechat.chat.ChatChannel;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

public class ClearChatCommand extends AbstractCommand {

    public ClearChatCommand() {
        super(true, "clear");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        ChatChannel channel = null;

        if (args.length == 0) {
            Player player = (Player) sender;
            channel = this.getAPI().getPlayerData(player.getUniqueId()).getCurrentChannel();
        } else if (args.length == 1) {
            for (ChatChannel chatChannel : this.getAPI().getChannels()) {
                if (chatChannel.getId().equalsIgnoreCase(args[0])) {
                    channel = chatChannel;
                    break;
                }
            }
        }

        if (channel == null) {
            this.getAPI().getLocaleManager().sendMessage(sender, "command-channel-not-found");
            return;
        }

        //channel.message((Player) sender, StringUtils.repeat(" \n", 100), false);
        this.getAPI().getLocaleManager().sendMessage(sender, "command-chat-clear-cleared", StringPlaceholders.single("channel", channel.getId()));
        this.getAPI().getLocaleManager().sendMessage(Bukkit.getConsoleSender(), "command-chat-clear-cleared", StringPlaceholders.single("channel", channel.getId()));
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

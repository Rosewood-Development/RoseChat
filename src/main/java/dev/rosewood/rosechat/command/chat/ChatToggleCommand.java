package dev.rosewood.rosechat.command.chat;

import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

public class ChatToggleCommand extends AbstractCommand {

    public ChatToggleCommand() {
        super(true, "toggle");
    }

    @Override // /chat toggle global
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "invalid-arguments", StringPlaceholders.single("syntax", this.getSyntax()));
            return;
        }

        Channel channel = this.getAPI().getChannelById(args[0]);
        if (channel == null) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-channel-not-found");
            return;
        }

        if (!sender.hasPermission("rosechat.chat.toggle." + channel.getId())) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "no-permission");
            return;
        }

        Player player = (Player) sender;
        PlayerData data = this.getAPI().getPlayerData(player.getUniqueId());

        if (data.isChannelHidden(channel.getId())) {
            data.showChannel(channel.getId());
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-chat-toggle-on",
                    StringPlaceholders.single("channel", channel.getId()));
        } else {
            data.hideChannel(channel.getId());
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-chat-toggle-off",
                    StringPlaceholders.single("channel", channel.getId()));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();

        if (args.length == 1) {
            for (String id : this.getAPI().getChannelIDs()) {
                if (!sender.hasPermission("rosechat.chat.toggle." + id)) continue;
                tab.add(id);
            }
        }

        return tab;
    }

    @Override
    public String getPermission() {
        return "rosechat.chat.toggle";
    }

    @Override
    public String getSyntax() {
        return this.getAPI().getLocaleManager().getLocaleMessage("command-chat-toggle-usage");
    }

}

package dev.rosewood.rosechat.command.chat;

import dev.rosewood.rosechat.chat.ChatChannel;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.command.CommandSender;
import java.util.ArrayList;
import java.util.List;

public class MuteChatCommand extends AbstractCommand {

    public MuteChatCommand() {
        super(false, "mute");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "invalid-arguments", StringPlaceholders.single("syntax", this.getSyntax()));
            return;
        }

        ChatChannel channel = this.getAPI().getChannelById(args[0]);
        if (channel == null) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-channel-not-found");
            return;
        }

        channel.setMuted(!channel.isMuted());

        if (channel.isMuted()) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-chat-mute-muted", StringPlaceholders.single("channel", channel.getId()));
            this.getAPI().getPlayerDataManager().addMutedChannel(channel);
        } else {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-chat-mute-unmuted", StringPlaceholders.single("channel", channel.getId()));
            this.getAPI().getPlayerDataManager().removeMutedChannel(channel);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();

        if (args.length == 1) {
            tab.addAll(this.getAPI().getChannelIDs());
        }

        return tab;
    }

    @Override
    public String getPermission() {
        return "rosechat.admin.mute";
    }

    @Override
    public String getSyntax() {
        return this.getAPI().getLocaleManager().getLocaleMessage("command-chat-mute-usage");
    }

}

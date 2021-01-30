package dev.rosewood.rosechat.commands.chat;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.chat.ChatChannel;
import dev.rosewood.rosechat.floralapi.AbstractCommand;
import dev.rosewood.rosechat.managers.ChannelManager;
import dev.rosewood.rosechat.managers.LocaleManager;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.command.CommandSender;
import java.util.ArrayList;
import java.util.List;

public class CommandChatMute extends AbstractCommand {

    private RoseChat plugin;
    private LocaleManager localeManager;
    private ChannelManager channelManager;

    public CommandChatMute(RoseChat plugin) {
        super(true, "mute");
        this.plugin = plugin;
        this.localeManager = plugin.getManager(LocaleManager.class);
        this.channelManager = plugin.getManager(ChannelManager.class);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            this.localeManager.sendMessage(sender, "invalid-arguments", StringPlaceholders.single("syntax", this.getSyntax()));
            return;
        }

        ChatChannel channel = this.channelManager.getChannel(args[0]);
        if (channel == null) {
            this.localeManager.sendMessage(sender, "command-channel-not-found");
            return;
        }

        channel.setMuted(!channel.isMuted());

        if (channel.isMuted()) {
            this.localeManager.sendMessage(sender, "command-chat-mute-muted", StringPlaceholders.single("channel", channel.getId()));
        } else {
            this.localeManager.sendMessage(sender, "command-chat-mute-unmuted", StringPlaceholders.single("channel", channel.getId()));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();

        if (args.length == 1) {
            tab.addAll(this.channelManager.getChannels().keySet());
        }

        return tab;
    }

    @Override
    public String getPermission() {
        return "rosechat.admin.mute";
    }

    @Override
    public String getSyntax() {
        return this.localeManager.getLocaleMessage("command-chat-mute-usage");
    }
}

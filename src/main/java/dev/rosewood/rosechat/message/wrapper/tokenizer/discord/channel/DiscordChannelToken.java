package dev.rosewood.rosechat.message.wrapper.tokenizer.discord.channel;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.Group;
import dev.rosewood.rosechat.hook.discord.DiscordChatProvider;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class DiscordChannelToken extends Token {

    private final MessageWrapper messageWrapper;
    private final Group group;
    private final String channelId;

    public DiscordChannelToken(MessageWrapper messageWrapper, Group group, RoseSender sender, RoseSender viewer, String originalContent, String channelId) {
        super(sender, viewer, originalContent);
        this.messageWrapper = messageWrapper;
        this.group = group;
        this.channelId = channelId;
    }

    @Override
    public BaseComponent[] toComponents() {
        DiscordChatProvider discord = RoseChatAPI.getInstance().getDiscord();
        String channelName = null;
        String serverId = null;

        if (discord != null) {
            channelName = discord.getChannelName(this.channelId);
            serverId = discord.getServerId();
        }

        ComponentBuilder componentBuilder = new ComponentBuilder();
        String placeholder = Setting.DISCORD_FORMAT_CHANNEL.getString();
        BaseComponent[] components = MessageUtils.parseCustomPlaceholder(this.getSender(), this.getSender(), placeholder.substring(1, placeholder.length() - 1),
                MessageUtils.getSenderViewerPlaceholders(this.getSender(), this.getViewer(), this.group, this.messageWrapper.getPlaceholders())
                        .addPlaceholder("channel_name", channelName)
                        .addPlaceholder("server_id", serverId)
                        .addPlaceholder("channel_id", this.channelId).build());

        if (components != null) componentBuilder.append(components);
        return componentBuilder.create();
    }
}

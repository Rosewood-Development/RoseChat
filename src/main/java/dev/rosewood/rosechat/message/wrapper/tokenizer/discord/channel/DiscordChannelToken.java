package dev.rosewood.rosechat.message.wrapper.tokenizer.discord.channel;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.Group;
import dev.rosewood.rosechat.hook.discord.DiscordChatProvider;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.manager.PlaceholderManager;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizers;
import dev.rosewood.rosechat.placeholders.CustomPlaceholder;
import dev.rosewood.rosegarden.hook.PlaceholderAPIHook;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

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

        PlaceholderManager placeholderManager = RoseChatAPI.getInstance().getPlaceholderManager();
        CustomPlaceholder placeholder = placeholderManager.getPlaceholder(Setting.DISCORD_FORMAT_CHANNEL.getString().replace("{", "").replace("}", ""));
        if (placeholder == null) return null;

        StringPlaceholders placeholders = MessageUtils.getSenderViewerPlaceholders(this.getSender(), this.getViewer(), this.group, this.messageWrapper.getPlaceholders())
                .addPlaceholder("channel_name", channelName)
                .addPlaceholder("server_id", serverId)
                .addPlaceholder("channel_id", this.channelId).build();

        BaseComponent[] components = null;
        HoverEvent hoverEvent = null;
        ClickEvent clickEvent = null;

        if (placeholder.getText() != null) {
            String text = placeholders.apply(placeholder.getText().parse(this.getSender(), this.getViewer(), placeholders)) + "&r";
            MessageTokenizer textTokenizer = new MessageTokenizer.Builder()
                    .group(this.group).sender(this.getSender()).viewer(this.getViewer()).location(MessageLocation.NONE)
                    .tokenizers(Tokenizers.TAG_TOKENIZERS).colorize(false).tokenize(text);
            components = textTokenizer.toComponents();
        }

        String hoverString = placeholder.getHover() != null ? placeholders.apply(placeholder.getHover().parse(this.getSender(), this.getViewer(), placeholders)) : null;
        if (hoverString != null) {
            MessageTokenizer hoverTokenizer = new MessageTokenizer.Builder()
                    .group(this.group).sender(this.getSender()).viewer(this.getViewer()).location(MessageLocation.OTHER)
                    .tokenizers(Tokenizers.TAG_TOKENIZERS).tokenize(hoverString);
            BaseComponent[] hover = hoverTokenizer.toComponents();
            hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover);
        }

        String clickString = placeholder.getClick() != null ? placeholders.apply(placeholder.getClick().parse(this.getSender(), this.getViewer(), placeholders)) : null;
        ClickEvent.Action action = placeholder.getClick() != null ? placeholder.getClick().parseToAction(this.getSender(), this.getViewer(), placeholders) : null;
        if (clickString != null && action != null) {
            String click = this.getSender().isPlayer() ? PlaceholderAPIHook.applyPlaceholders(this.getSender().asPlayer(), clickString) : clickString;
            if (click != null) clickEvent = new ClickEvent(action, HexUtils.colorify(click));
        }

        ComponentBuilder componentBuilder = new ComponentBuilder();

        for (BaseComponent component : components) {
            for (char c : component.toPlainText().toCharArray()) {
                componentBuilder.append(String.valueOf(c), ComponentBuilder.FormatRetention.NONE).event(hoverEvent).event(clickEvent);
            }
        }
        return componentBuilder.create();
    }
}

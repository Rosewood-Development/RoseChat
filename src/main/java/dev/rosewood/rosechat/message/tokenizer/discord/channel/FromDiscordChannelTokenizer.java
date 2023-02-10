package dev.rosewood.rosechat.message.tokenizer.discord.channel;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.hook.discord.DiscordChatProvider;
import dev.rosewood.rosechat.manager.ConfigurationManager;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.Tokenizers;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import java.util.regex.Matcher;

public class FromDiscordChannelTokenizer implements Tokenizer<Token> {

    @Override
    public Token tokenize(RoseMessage roseMessage, RosePlayer viewer, String input, boolean ignorePermissions) {
        if (!this.hasPermission(roseMessage, ignorePermissions, "rosechat.channel")) return null;
        if (!input.startsWith("<")) return null;

        Matcher matcher = MessageUtils.DISCORD_CHANNEL_PATTERN.matcher(input);
        if (matcher.find()) {
            if (matcher.start() != 0) return null;
            String originalContent = input.substring(0, matcher.end());

            DiscordChatProvider discord = RoseChatAPI.getInstance().getDiscord();
            if (discord == null) return null;
            String channelName = discord.getChannelName(matcher.group(1));
            String serverId = discord.getServerId();
            String content = ConfigurationManager.Setting.DISCORD_FORMAT_CHANNEL.getString();

            return new Token(new Token.TokenSettings(originalContent).content(content).hoverAction(HoverEvent.Action.SHOW_TEXT).clickAction(ClickEvent.Action.OPEN_URL)
                    .placeholder("server_id", serverId).placeholder("channel_id", matcher.group(1)).placeholder("channel_name", channelName).ignoreTokenizer(this).ignoreTokenizer(Tokenizers.TAG));
        }

        return null;
    }

    @Override
    public boolean isPerPlayer() {
        return false;
    }

}

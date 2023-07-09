package dev.rosewood.rosechat.message.tokenizer.discord.channel;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.hook.discord.DiscordChatProvider;
import dev.rosewood.rosechat.manager.ConfigurationManager;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import dev.rosewood.rosechat.message.tokenizer.Tokenizers;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FromDiscordChannelTokenizer extends Tokenizer {

    public static final Pattern PATTERN = Pattern.compile("<#([0-9]{18,19})>");

    public FromDiscordChannelTokenizer() {
        super("from_discord_channel");
    }

    @Override
    public TokenizerResult tokenize(TokenizerParams params) {
        String input = params.getInput();
        if (!input.startsWith("<")) return null;
        if (!MessageUtils.hasTokenPermission(params, "rosechat.discordchannel")) return null;

        Matcher matcher = PATTERN.matcher(input);
        if (!matcher.find() || matcher.start() != 0) return null;

        DiscordChatProvider discord = RoseChatAPI.getInstance().getDiscord();
        if (discord == null) return null;
        String channelName = discord.getChannelName(matcher.group(1));
        String serverId = discord.getServerId();
        String content = ConfigurationManager.Setting.DISCORD_FORMAT_CHANNEL.getString();

        return new TokenizerResult(Token.group(content)
                .placeholder("server_id", serverId)
                .placeholder("channel_id", matcher.group(1))
                .placeholder("channel_name", channelName)
                .ignoreTokenizer(this)
                .ignoreTokenizer(Tokenizers.TAG)
                .build(), matcher.group().length());
    }

}

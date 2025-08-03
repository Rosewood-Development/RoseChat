package dev.rosewood.rosechat.message.tokenizer.discord.channel;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.hook.discord.DiscordChatProvider;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import java.util.List;

public class ToDiscordChannelTokenizer extends Tokenizer {

    public ToDiscordChannelTokenizer() {
        super("to_discord_channel");
    }

    @Override
    public List<TokenizerResult> tokenize(TokenizerParams params) {
        String input = params.getInput();
        if (!input.startsWith("#"))
            return null;

        if (!this.hasTokenPermission(params, "rosechat.discordchannel"))
            return null;

        DiscordChatProvider discord = RoseChatAPI.getInstance().getDiscord();
        DiscordChatProvider.DetectedMention channel = discord.matchPartialChannel(input.substring(1));
        if (channel == null)
            return null;

        return List.of(new TokenizerResult(Token.text(channel.mention()), 0, input.length()));
    }

}

package dev.rosewood.rosechat.message.tokenizer.discord.channel;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.hook.discord.DiscordChatProvider;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import dev.rosewood.rosechat.message.tokenizer.Tokenizers;

public class ToDiscordChannelTokenizer extends Tokenizer {

    public ToDiscordChannelTokenizer() {
        super("to_discord_channel");
    }

    @Override
    public TokenizerResult tokenize(TokenizerParams params) {
        String input = params.getInput();
        if (!input.startsWith("#")) return null;
        if (!MessageUtils.hasTokenPermission(params, "rosechat.discordchannel")) return null;

        DiscordChatProvider discord = RoseChatAPI.getInstance().getDiscord();
        DiscordChatProvider.DetectedMention channel = discord.matchPartialChannel(input.substring(1));
        if (channel == null) return null;

        return new TokenizerResult(Token.text(channel.mention())
                .ignoreTokenizer(this)
                .ignoreTokenizer(Tokenizers.COLOR)
                .ignoreTokenizer(Tokenizers.SHADER_COLORS)
                .build(), input.length());
    }

}

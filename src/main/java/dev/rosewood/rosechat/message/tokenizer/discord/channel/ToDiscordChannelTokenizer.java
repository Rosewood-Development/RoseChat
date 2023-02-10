package dev.rosewood.rosechat.message.tokenizer.discord.channel;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.hook.discord.DiscordChatProvider;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.Tokenizers;

public class ToDiscordChannelTokenizer implements Tokenizer<Token> {

    @Override
    public Token tokenize(RoseMessage roseMessage, RosePlayer viewer, String input, boolean ignorePermissions) {
        if (!this.hasPermission(roseMessage, ignorePermissions, "rosechat.channel")) return null;
        if (!input.startsWith("#")) return null;

        DiscordChatProvider discord = RoseChatAPI.getInstance().getDiscord();
        DiscordChatProvider.DetectedMention channel = discord.matchPartialChannel(input.substring(1));
        if (channel == null) return null;

        return new Token(new Token.TokenSettings(input.substring(0, channel.getConsumedTextLength() + 1)).content(channel.getMention())
                .ignoreTokenizer(this).ignoreTokenizer(Tokenizers.TAG).ignoreTokenizer(Tokenizers.COLOR).ignoreTokenizer(Tokenizers.FORMAT));
    }

    @Override
    public boolean isPerPlayer() {
        return false;
    }

}

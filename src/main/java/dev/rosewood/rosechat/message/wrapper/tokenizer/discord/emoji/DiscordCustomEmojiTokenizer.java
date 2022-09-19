package dev.rosewood.rosechat.message.wrapper.tokenizer.discord.emoji;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizers;
import java.util.regex.Matcher;

public class DiscordCustomEmojiTokenizer implements Tokenizer<Token> {

    @Override
    public Token tokenize(MessageWrapper messageWrapper, RoseSender viewer, String input, boolean ignorePermissions) {
        if (!hasPermission(messageWrapper, ignorePermissions, "rosechat.emoji")) return null;
        if (!input.startsWith(":")) return null;
        Matcher matcher = MessageUtils.EMOJI_PATTERN.matcher(input);
        if (matcher.find()) {
            String originalContent = input.substring(matcher.start(), matcher.end());
            String content = matcher.group(1);
            content = RoseChatAPI.getInstance().getDiscord().getCustomEmoji(content);
            return new Token(new Token.TokenSettings(originalContent).content(content).ignoreTokenizer(this).ignoreTokenizer(Tokenizers.EMOJI));
        }

        return null;
    }

}

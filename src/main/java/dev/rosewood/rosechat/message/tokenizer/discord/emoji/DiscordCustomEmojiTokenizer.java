package dev.rosewood.rosechat.message.tokenizer.discord.emoji;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.Tokenizers;
import java.util.regex.Matcher;

public class DiscordCustomEmojiTokenizer implements Tokenizer<Token> {

    @Override
    public Token tokenize(RoseMessage roseMessage, RosePlayer viewer, String input, boolean ignorePermissions) {
        if (!input.startsWith(":")) return null;
        if (!ignorePermissions && !MessageUtils.hasTokenPermission(roseMessage, "rosechat.emojis")) return null;

        Matcher matcher = MessageUtils.EMOJI_PATTERN.matcher(input);
        if (matcher.find()) {
            if (matcher.start() != 0) return null;
            String originalContent = input.substring(0, matcher.end());
            String content = matcher.group(1);
            if (!ignorePermissions && !MessageUtils.hasExtendedTokenPermission(roseMessage, "rosechat.emojis", "rosechat.emoji." + content)) return null;
            content = RoseChatAPI.getInstance().getDiscord().getCustomEmoji(content);
            return new Token(new Token.TokenSettings(originalContent).content(content).ignoreTokenizer(this).ignoreTokenizer(Tokenizers.EMOJI).requiresTokenizing(false));
        }

        return null;
    }

}

package dev.rosewood.rosechat.message.tokenizer.discord.emoji;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiscordCustomEmojiTokenizer extends Tokenizer {

    public static final Pattern PATTERN = Pattern.compile(":([a-zA-Z_]+):");

    public DiscordCustomEmojiTokenizer() {
        super("discord_custom_emoji");
    }

    @Override
    public TokenizerResult tokenize(TokenizerParams params) {
        String input = params.getInput();
        if (!input.startsWith(":")) return null;
        if (!MessageUtils.hasTokenPermission(params, "rosechat.emojis")) return null;

        Matcher matcher = PATTERN.matcher(input);
        if (!matcher.find() || matcher.start() != 0) return null;

        String content = matcher.group(1);
        if (!MessageUtils.hasExtendedTokenPermission(params, "rosechat.emojis", "rosechat.emoji." + content)) return null;
        content = RoseChatAPI.getInstance().getDiscord().getCustomEmoji(content);
        return new TokenizerResult(Token.text(content).build(), matcher.group().length());
    }

}

package dev.rosewood.rosechat.message.tokenizer.discord.emoji;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.filter.Filter;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import dev.rosewood.rosechat.message.tokenizer.Tokenizers;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiscordEmojiTokenizer extends Tokenizer {

    public static final Pattern PATTERN = Pattern.compile("<a?(:[a-zA-Z0-9_\\-~]+:)[0-9]{18,19}>");

    public DiscordEmojiTokenizer() {
        super("discord_emoji");
    }

    @Override
    public TokenizerResult tokenize(TokenizerParams params) {
        String input = params.getInput();
        if (!input.startsWith("<"))
            return null;

        Matcher matcher = PATTERN.matcher(input);
        if (!matcher.find() || matcher.start() != 0)
            return null;

        String content = matcher.group(1);
        for (Filter filter : RoseChatAPI.getInstance().getFilters()) {
            if (filter.matches().isEmpty())
                continue;

            for (String match : filter.matches()) {
                if (!match.equals(content))
                    continue;

                if (filter.usePermission() != null) {
                    if (!this.hasExtendedTokenPermission(params, "rosechat.filters", filter.usePermission()))
                        return null;
                }

                content = filter.replacement();
                return new TokenizerResult(Token.group(content)
                        .ignoreTokenizer(this)
                        .ignoreTokenizer(Tokenizers.FILTER)
                        .build(), matcher.group().length());
            }
        }

        return new TokenizerResult(Token.text(matcher.group(1)), matcher.group().length());
    }

}

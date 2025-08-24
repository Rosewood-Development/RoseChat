package dev.rosewood.rosechat.message.tokenizer.discord.emoji;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.filter.Filter;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import dev.rosewood.rosechat.message.tokenizer.Tokenizers;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiscordEmojiTokenizer extends Tokenizer {

    public static final Pattern PATTERN = Pattern.compile("<a?(:[a-zA-Z0-9_\\-~]+:)[0-9]{18,19}>");

    public DiscordEmojiTokenizer() {
        super("discord_emoji");
    }

    @Override
    public List<TokenizerResult> tokenize(TokenizerParams params) {
        if (true) return null;
        String rawInput = params.getInput();
        String input = rawInput.charAt(0) == MessageUtils.ESCAPE_CHAR ? rawInput.substring(1) : rawInput;
        if (rawInput.charAt(0) == MessageUtils.ESCAPE_CHAR && !params.getSender().hasPermission("rosechat.escape"))
            return null;

        if (!input.startsWith("<"))
            return null;

        Matcher matcher = PATTERN.matcher(input);
        if (!matcher.find() || matcher.start() != 0)
            return null;

        if (rawInput.charAt(0) == MessageUtils.ESCAPE_CHAR)
            return List.of(new TokenizerResult(Token.text(input), input.length() + 1));

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
                return List.of(new TokenizerResult(Token.group(content)
                        .ignoreTokenizer(this)
                        .ignoreTokenizer(Tokenizers.FILTER)
                        .build(), matcher.group().length()));
            }
        }

        return List.of(new TokenizerResult(Token.text(matcher.group(1)), matcher.group().length()));
    }

}

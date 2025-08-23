package dev.rosewood.rosechat.message.tokenizer.discord;

import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ToDiscordURLTokenizer extends Tokenizer {

    public static final Pattern PATTERN = Pattern.compile("^\\[([\\w\\s\\d]+)\\]\\(((?:\\/|https?:\\/\\/)?[\\w\\d./?=#]+)\\)$");

    public ToDiscordURLTokenizer() {
        super("to_discord_url");
    }

    @Override
    public TokenizerResult tokenize(TokenizerParams params) {
        String rawInput = params.getInput();
        String input = rawInput.charAt(0) == MessageUtils.ESCAPE_CHAR ? rawInput.substring(1) : rawInput;
        if (rawInput.charAt(0) == MessageUtils.ESCAPE_CHAR && !params.getSender().hasPermission("rosechat.escape"))
            return null;

        if (!input.startsWith("["))
            return null;

        if (!this.hasTokenPermission(params, "rosechat.url"))
            return null;

        Matcher matcher = PATTERN.matcher(input);
        if (!matcher.find() || matcher.start() != 0)
            return null;

        if (rawInput.charAt(0) == MessageUtils.ESCAPE_CHAR)
            return new TokenizerResult(Token.text(input), input.length() + 1);

        String markdown = matcher.group();
        return List.of(new TokenizerResult(Token.text(markdown), 0, markdown.length()));
    }

}

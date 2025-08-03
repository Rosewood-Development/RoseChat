package dev.rosewood.rosechat.message.tokenizer.discord;

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
    public List<TokenizerResult> tokenize(TokenizerParams params) {
        String input = params.getInput();
        if (!input.startsWith("["))
            return null;

        if (!this.hasTokenPermission(params, "rosechat.url"))
            return null;

        Matcher matcher = PATTERN.matcher(input);
        if (!matcher.find() || matcher.start() != 0)
            return null;

        String markdown = matcher.group();
        return List.of(new TokenizerResult(Token.text(markdown), 0, markdown.length()));
    }

}

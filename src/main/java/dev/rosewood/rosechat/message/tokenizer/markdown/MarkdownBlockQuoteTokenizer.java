package dev.rosewood.rosechat.message.tokenizer.markdown;

import dev.rosewood.rosechat.config.Settings;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownBlockQuoteTokenizer extends Tokenizer {

    private static final Pattern PATTERN = Pattern.compile(Pattern.quote("%message%"));

    public MarkdownBlockQuoteTokenizer() {
        super("markdown_block_quote");
    }

    @Override
    public TokenizerResult tokenize(TokenizerParams params) {
        String playerInput = params.getPlayerInput();
        if (playerInput == null || !params.getPlayerInput().startsWith("> "))
            return null;

        String input = params.getInput();
        if (!input.startsWith("> "))
            return null;

        if (!this.hasTokenPermission(params, "rosechat.quote"))
            return null;

        String content = input.substring(2);
        String format = Settings.MARKDOWN_FORMAT_BLOCK_QUOTES.get();

        if (!format.contains("%message%")) {
            return new TokenizerResult(Token.group(
                    Token.group(format).ignoreTokenizer(this).build(),
                    Token.group(content).ignoreTokenizer(this).containsPlayerInput().build()
            ).build(), input.length());
        }

        Matcher matcher = PATTERN.matcher(format);
        List<Token> chunks = new ArrayList<>();
        int contentIndex = 0;
        while (matcher.find()) {
            if (contentIndex != matcher.start())
                chunks.add(Token.group(format.substring(contentIndex, matcher.start())).ignoreTokenizer(this).build());
            chunks.add(Token.group(content).ignoreTokenizer(this).containsPlayerInput().build());
            contentIndex = matcher.end();
        }

        if (contentIndex < format.length())
            chunks.add(Token.group(format.substring(contentIndex)).ignoreTokenizer(this).build());

        return new TokenizerResult(Token.group(chunks).build(), input.length());
    }

}

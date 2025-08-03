package dev.rosewood.rosechat.message.tokenizer.markdown;

import dev.rosewood.rosechat.config.Settings;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownStrikethroughTokenizer extends Tokenizer {

    public static final Pattern PATTERN = Pattern.compile("~~(?=\\S)([\\s\\S]*?\\S)~~");

    public MarkdownStrikethroughTokenizer() {
        super("markdown_strikethrough");
    }

    @Override
    public List<TokenizerResult> tokenize(TokenizerParams params) {
        String input = params.getInput();
        if (!input.startsWith("~~"))
            return null;

        Matcher matcher = PATTERN.matcher(input);
        if (!matcher.find() || matcher.start() != 0)
            return null;

        if (!this.hasTokenPermission(params, "rosechat.strikethrough"))
            return null;

        String originalContent = input.substring(0, matcher.end());
        String content = originalContent.substring(2, originalContent.length() - 2);
        String format = Settings.MARKDOWN_FORMAT_STRIKETHROUGH.get();

        if (!format.contains("%input_1%")) {
            return List.of(new TokenizerResult(Token.group(
                    Token.group(format).ignoreTokenizer(this).build(),
                    Token.group(content).ignoreTokenizer(this).containsPlayerInput().build()
            ).build(), 0, originalContent.length()));
        }

        return List.of(new TokenizerResult(Token.group(format)
                .placeholder("input_1", content)
                .ignoreTokenizer(this)
                .build(), 0, originalContent.length()));
    }

}

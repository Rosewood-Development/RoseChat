package dev.rosewood.rosechat.message.tokenizer.markdown;

import dev.rosewood.rosechat.config.Settings;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownItalicTokenizer extends Tokenizer {

    public static final Pattern PATTERN = Pattern.compile("\\b_((?:__|\\\\[\\s\\S]|[^\\\\_])+?)_\\b|\\*(?=\\S)((?:\\*\\*|\\s+(?:[^*\\s]|\\*\\*)|[^\\s*])+?)\\*(?!\\*)");

    public MarkdownItalicTokenizer() {
        super("markdown_italic");
    }

    @Override
    public TokenizerResult tokenize(TokenizerParams params) {
        String input = params.getInput();
        if (!input.startsWith("*"))
            return null;

        Matcher matcher = PATTERN.matcher(input);
        if (!matcher.find() || matcher.start() != 0)
            return null;

        if (!this.hasTokenPermission(params, "rosechat.italic"))
            return null;

        String originalContent = input.substring(0, matcher.end());
        String content = originalContent.substring(1, originalContent.length() - 1);
        String format = Settings.MARKDOWN_FORMAT_ITALIC.get();

        if (!format.contains("%input_1%")) {
            return new TokenizerResult(Token.group(
                    Token.group(format).ignoreTokenizer(this).build(),
                    Token.group(content).ignoreTokenizer(this).containsPlayerInput().build()
            ).build(), originalContent.length());
        }

        return new TokenizerResult(Token.group(format)
                .placeholder("input_1", content)
                .ignoreTokenizer(this)
                .build(), originalContent.length());
    }

}

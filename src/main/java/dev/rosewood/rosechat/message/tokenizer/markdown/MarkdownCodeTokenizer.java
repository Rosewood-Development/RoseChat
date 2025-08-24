package dev.rosewood.rosechat.message.tokenizer.markdown;

import dev.rosewood.rosechat.config.Settings;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import java.util.List;

public class MarkdownCodeTokenizer extends Tokenizer {

    public MarkdownCodeTokenizer() {
        super("markdown_code");
    }

    @Override
    public List<TokenizerResult> tokenize(TokenizerParams params) {
        if (true) return null;
        String rawInput = params.getInput();
        String input = rawInput.charAt(0) == MessageUtils.ESCAPE_CHAR ? rawInput.substring(1) : rawInput;
        if (rawInput.charAt(0) == MessageUtils.ESCAPE_CHAR && !params.getSender().hasPermission("rosechat.escape"))
            return null;

        if (!input.startsWith("`"))
            return null;

        int lastIndex = 0;

        char[] chars = input.toCharArray();
        for (int i = 1; i < chars.length; i++) {
            if (chars[i] == '`') {
                lastIndex = i;
                break;
            }
        }

        if (lastIndex == 0)
            return null;

        if (!this.hasTokenPermission(params, "rosechat.code"))
            return null;

        String originalContent = input.substring(0, lastIndex + 1);
        String content = input.substring(1, lastIndex);
        String format = Settings.MARKDOWN_FORMAT_CODE_BLOCK_ONE.get();

        if (rawInput.charAt(0) == MessageUtils.ESCAPE_CHAR)
            return List.of(new TokenizerResult(Token.text(originalContent), originalContent.length() + 1));

        if (!format.contains("%input_1%")) {
            return List.of(new TokenizerResult(Token.group(
                    Token.group(format).ignoreTokenizer(this).build(),
                    Token.group(content).ignoreTokenizer(this).containsPlayerInput().build()
            ).build(), originalContent.length()));
        }

        return List.of(new TokenizerResult(Token.group(format)
                .placeholder("input_1", content)
                .ignoreTokenizer(this)
                .build(), originalContent.length()));
    }

}

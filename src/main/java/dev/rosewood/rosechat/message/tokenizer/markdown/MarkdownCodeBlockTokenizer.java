package dev.rosewood.rosechat.message.tokenizer.markdown;

import dev.rosewood.rosechat.config.Settings;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import dev.rosewood.rosechat.message.tokenizer.Tokenizers;
import java.util.List;

public class MarkdownCodeBlockTokenizer extends Tokenizer {

    public MarkdownCodeBlockTokenizer() {
        super("markdown_code_block");
    }

    @Override
    public List<TokenizerResult> tokenize(TokenizerParams params) {
        String input = params.getInput();
        if (!input.startsWith("```"))
            return null;

        int lastIndex = 0;

        char[] chars = input.toCharArray();
        for (int i = 3; i < chars.length; i++) {
            if (chars.length - 2 > i && chars[i] == '`' && chars[i + 1] == '`' && chars[i + 2] == '`') {
                lastIndex = i + 3;
                break;
            }
        }

        if (lastIndex == 0)
            return null;

        if (!this.hasTokenPermission(params, "rosechat.multicode"))
            return null;

        String originalContent = input.substring(0, lastIndex);
        String content = input.substring(3, lastIndex - 3);
        String format = Settings.MARKDOWN_FORMAT_CODE_BLOCK_MULTIPLE.get();

        if (!format.contains("%input_1%")) {
            return List.of(new TokenizerResult(Token.group(
                    Token.group(format).ignoreTokenizer(this).ignoreTokenizer(Tokenizers.MARKDOWN_CODE).build(),
                    Token.group(content).ignoreTokenizer(this).ignoreTokenizer(Tokenizers.MARKDOWN_CODE).containsPlayerInput().build()
            ).build(), 0, originalContent.length()));
        }

        return List.of(new TokenizerResult(Token.group(format)
                .placeholder("input_1", content)
                .ignoreTokenizer(this)
                .build(), 0, originalContent.length()));
    }

}

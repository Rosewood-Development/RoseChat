package dev.rosewood.rosechat.message.tokenizer.markdown;

import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import dev.rosewood.rosechat.message.tokenizer.Tokenizers;

public class MarkdownCodeBlockTokenizer extends Tokenizer {

    public MarkdownCodeBlockTokenizer() {
        super("markdown_code_block");
    }

    @Override
    public TokenizerResult tokenize(TokenizerParams params) {
        String input = params.getInput();
        if (!input.startsWith("```"))
            return null;
        if (!MessageUtils.hasTokenPermission(params, "rosechat.multicode"))
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

        String originalContent = input.substring(0, lastIndex);
        String content = input.substring(3, lastIndex - 3);

        String format = Setting.MARKDOWN_FORMAT_CODE_BLOCK_MULTIPLE.getString();
        content = format.contains("%message%") ? format.replace("%message%", content) : format + content;

        return new TokenizerResult(Token.group(content).ignoreTokenizer(this).ignoreTokenizer(Tokenizers.MARKDOWN_CODE).build(), originalContent.length());
    }

}

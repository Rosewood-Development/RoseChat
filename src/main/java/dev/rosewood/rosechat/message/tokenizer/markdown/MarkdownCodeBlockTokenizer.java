package dev.rosewood.rosechat.message.tokenizer.markdown;

import dev.rosewood.rosechat.config.Settings;
import dev.rosewood.rosechat.message.tokenizer.Tokenizers;
import java.util.regex.Pattern;

public class MarkdownCodeBlockTokenizer extends BaseMarkdownTokenizer {

    private static final Pattern PATTERN = Pattern.compile("```([^`]+?)```");

    public MarkdownCodeBlockTokenizer() {
        super("markdown_code_block", PATTERN, "rosechat.multicode", Settings.MARKDOWN_FORMAT_CODE_BLOCK_MULTIPLE, Tokenizers.MARKDOWN_CODE);
    }

}

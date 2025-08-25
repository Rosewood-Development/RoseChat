package dev.rosewood.rosechat.message.tokenizer.markdown;

import dev.rosewood.rosechat.config.Settings;
import java.util.regex.Pattern;

public class MarkdownCodeTokenizer extends BaseMarkdownTokenizer {

    private static final Pattern PATTERN = Pattern.compile("`([^`]+?)`");

    public MarkdownCodeTokenizer() {
        super("markdown_code", PATTERN, "rosechat.code", Settings.MARKDOWN_FORMAT_CODE_BLOCK_ONE);
    }

}

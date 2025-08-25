package dev.rosewood.rosechat.message.tokenizer.markdown;

import dev.rosewood.rosechat.config.Settings;
import java.util.regex.Pattern;

public class MarkdownStrikethroughTokenizer extends BaseMarkdownTokenizer {

    public static final Pattern PATTERN = Pattern.compile("~~(?=\\S)([\\s\\S]*?\\S)~~");

    public MarkdownStrikethroughTokenizer() {
        super("markdown_strikethrough", PATTERN, "rosechat.strikethrough", Settings.MARKDOWN_FORMAT_STRIKETHROUGH);
    }

}

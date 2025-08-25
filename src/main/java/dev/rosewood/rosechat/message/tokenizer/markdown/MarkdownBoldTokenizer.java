package dev.rosewood.rosechat.message.tokenizer.markdown;

import dev.rosewood.rosechat.config.Settings;
import java.util.regex.Pattern;

public class MarkdownBoldTokenizer extends BaseMarkdownTokenizer {

    public static final Pattern PATTERN = Pattern.compile("\\*\\*([\\s\\S]+?)\\*\\*(?!\\*)");

    public MarkdownBoldTokenizer() {
        super("markdown_bold", PATTERN, "rosechat.bold", Settings.MARKDOWN_FORMAT_BOLD);
    }

}

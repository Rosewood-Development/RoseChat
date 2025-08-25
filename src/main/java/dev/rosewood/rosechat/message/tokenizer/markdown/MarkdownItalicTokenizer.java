package dev.rosewood.rosechat.message.tokenizer.markdown;

import dev.rosewood.rosechat.config.Settings;
import dev.rosewood.rosechat.message.MessageUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownItalicTokenizer extends BaseMarkdownTokenizer {

    public static final Pattern PATTERN = Pattern.compile("\\b_((?:__|\\\\[\\s\\S]|[^\\\\_])+?)_\\b|\\*(?=\\S)((?:\\*\\*|\\s+(?:[^*\\s]|\\*\\*)|[^\\s*])+?)\\*(?!\\*)");

    public MarkdownItalicTokenizer() {
        super("markdown_italic", PATTERN, "rosechat.italic", Settings.MARKDOWN_FORMAT_ITALIC);
    }

    @Override
    protected boolean isPlayerName(String content) {
        return MessageUtils.getPlayer("_" + content + "_") != null;
    }

    @Override
    protected String getContent(Matcher matcher) {
        String match = matcher.group();
        return match.substring(1, match.length() - 1);
    }

}

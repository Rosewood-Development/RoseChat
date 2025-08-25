package dev.rosewood.rosechat.message.tokenizer.markdown;

import dev.rosewood.rosechat.config.Settings;
import dev.rosewood.rosechat.message.MessageUtils;
import java.util.regex.Pattern;

public class MarkdownUnderlineTokenizer extends BaseMarkdownTokenizer {

    public static final Pattern PATTERN = Pattern.compile("__([\\s\\S]+?)__(?!_)");

    public MarkdownUnderlineTokenizer() {
        super("markdown_underline", PATTERN, "rosechat.underline", Settings.MARKDOWN_FORMAT_UNDERLINE);
    }

    @Override
    protected boolean isPlayerName(String content) {
        return MessageUtils.getPlayer("__" + content + "__") != null;
    }

}

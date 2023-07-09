package dev.rosewood.rosechat.message.tokenizer.markdown;

import dev.rosewood.rosechat.manager.ConfigurationManager;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownUnderlineTokenizer extends Tokenizer {

    public static final Pattern PATTERN = Pattern.compile("__([\\s\\S]+?)__(?!_)");

    public MarkdownUnderlineTokenizer() {
        super("markdown_underline");
    }

    @Override
    public TokenizerResult tokenize(TokenizerParams params) {
        String input = params.getInput();
        if (!MessageUtils.hasTokenPermission(params, "rosechat.underline")) return null;
        if (!input.startsWith("__")) return null;

        Matcher matcher = PATTERN.matcher(input);
        if (!matcher.find() || matcher.start() != 0) return null;

        String originalContent = input.substring(0, matcher.end());
        String content = originalContent.substring(2, originalContent.length() - 2);

        String format = ConfigurationManager.Setting.MARKDOWN_FORMAT_UNDERLINE.getString();
        content = format.contains("%message%") ? format.replace("%message%", content) : format + content;
        return new TokenizerResult(Token.group(content).ignoreTokenizer(this).build(), originalContent.length());
    }

}

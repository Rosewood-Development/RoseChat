package dev.rosewood.rosechat.message.tokenizer.markdown;

import dev.rosewood.rosechat.manager.ConfigurationManager;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownURLTokenizer extends Tokenizer {

    public static final Pattern PATTERN = Pattern.compile("^\\[(.+)]\\(((https?://)?[-a-zA-Z0-9@:%._+~#=]{2,32}\\.[a-zA-Z0-9()]{2,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*))\\)");

    public MarkdownURLTokenizer() {
        super("markdown_url");
    }

    @Override
    public TokenizerResult tokenize(TokenizerParams params) {
        String input = params.getInput();
        if (!input.startsWith("[")) return null;

        if (!MessageUtils.hasTokenPermission(params, "rosechat.url"))
            return null;

        Matcher matcher = PATTERN.matcher(input);
        if (!matcher.find() || matcher.start() != 0) return null;

        String originalContent = matcher.group();
        String content = matcher.group(1);
        String url = matcher.group(2);
        url = url.startsWith("http") ? url : "https://" + url;

        return new TokenizerResult(Token.group(ConfigurationManager.Setting.MARKDOWN_FORMAT_URL.getString())
                .placeholder("message", content)
                .placeholder("extra", url)
                .encapsulate()
                .build(), originalContent.length());
    }

}

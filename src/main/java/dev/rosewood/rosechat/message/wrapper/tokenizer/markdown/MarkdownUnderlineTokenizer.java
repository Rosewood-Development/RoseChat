package dev.rosewood.rosechat.message.wrapper.tokenizer.markdown;

import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;
import java.util.regex.Matcher;

public class MarkdownUnderlineTokenizer implements Tokenizer<Token> {

    @Override
    public Token tokenize(MessageWrapper messageWrapper, RoseSender viewer, String input, boolean ignorePermissions) {
        if (!hasPermission(messageWrapper, ignorePermissions, "rosechat.")) return null;
        if (!input.startsWith("__")) return null;

        Matcher matcher = MessageUtils.UNDERLINE_MARKDOWN_PATTERN.matcher(input);
        if (matcher.find()) {
            String originalContent = input.substring(matcher.start(), matcher.end());
            String content = originalContent.substring(2, originalContent.length() - 2);

            return new Token(new Token.TokenSettings(originalContent).content("&n" + content + "&N").ignoreTokenizer(this));
        }

        return null;
    }

}

package dev.rosewood.rosechat.message.wrapper.tokenizer.markdown;

import dev.rosewood.rosechat.manager.ConfigurationManager;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;
import java.util.regex.Matcher;

public class MarkdownItalicTokenizer implements Tokenizer<Token> {

    @Override
    public Token tokenize(MessageWrapper messageWrapper, RoseSender viewer, String input, boolean ignorePermissions) {
        if (!hasPermission(messageWrapper, ignorePermissions, "rosechat.italic")) return null;
        if (!input.startsWith("*")) return null;

        Matcher matcher = MessageUtils.ITALIC_MARKDOWN_PATTERN.matcher(input);
        if (matcher.find()) {
            String originalContent = input.substring(matcher.start(), matcher.end());
            String content = originalContent.substring(1, originalContent.length() - 1);

            String format = ConfigurationManager.Setting.MARKDOWN_FORMAT_ITALIC.getString();
            content = format.contains("%message%") ? format.replace("%message%", content) : format + content;
            return new Token(new Token.TokenSettings(originalContent).content(content).ignoreTokenizer(this));
        }

        return null;
    }

}

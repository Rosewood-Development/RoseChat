package dev.rosewood.rosechat.message.wrapper.tokenizer.url;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.manager.ConfigurationManager;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;
import dev.rosewood.rosechat.placeholders.CustomPlaceholder;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import java.util.regex.Matcher;

public class URLTokenizer implements Tokenizer<Token> {

    @Override
    public Token tokenize(MessageWrapper messageWrapper, RoseSender viewer, String input, boolean ignorePermissions) {
        if (input.startsWith("[")) {
            Matcher matcher = MessageUtils.URL_MARKDOWN_PATTERN.matcher(input);
            if (matcher.find()) {
                if (!hasPermission(messageWrapper, ignorePermissions, "rosechat.url")) return null;

                String originalContent = input.substring(matcher.start(), matcher.end());
                String content = matcher.group(1);

                String url = matcher.group(2);
                url = url.startsWith("http") ? url : "https://" + url;

                return new Token(new Token.TokenSettings(originalContent).content(ConfigurationManager.Setting.MARKDOWN_FORMAT_URL.getKey())
                        .hoverAction(HoverEvent.Action.SHOW_TEXT).clickAction(ClickEvent.Action.OPEN_URL)
                        .placeholder("message", content).placeholder("extra", url).ignoreTokenizer(this));
            }
        }

        return null;
    }

}

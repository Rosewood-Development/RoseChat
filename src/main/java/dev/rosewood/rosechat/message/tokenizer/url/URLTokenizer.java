package dev.rosewood.rosechat.message.tokenizer.url;

import dev.rosewood.rosechat.manager.ConfigurationManager;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import java.util.regex.Matcher;

public class URLTokenizer implements Tokenizer<Token> {

    @Override
    public Token tokenize(RoseMessage roseMessage, RosePlayer viewer, String input, boolean ignorePermissions) {
        if (input.startsWith("[")) {
            Matcher matcher = MessageUtils.URL_MARKDOWN_PATTERN.matcher(input);
            if (matcher.find()) {
                if (matcher.start() != 0) return null;
                if (!ignorePermissions && !MessageUtils.hasTokenPermission(roseMessage, "rosechat.url"))
                    return null;

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

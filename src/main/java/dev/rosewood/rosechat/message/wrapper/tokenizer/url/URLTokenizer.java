package dev.rosewood.rosechat.message.wrapper.tokenizer.url;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;
import dev.rosewood.rosechat.placeholders.CustomPlaceholder;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import java.util.regex.Matcher;

public class URLTokenizer implements Tokenizer<URLToken> {

    @Override
    public URLToken tokenize(MessageWrapper messageWrapper, RoseSender viewer, String input) {
        if (input.startsWith("[")) {
            Matcher matcher = MessageUtils.URL_MARKDOWN_PATTERN.matcher(input);
            if (matcher.find()) {
                String originalContent = input.substring(matcher.start(), matcher.end());
                String content = matcher.group(1);


                String url = matcher.group(2);
                url = url.startsWith("http") ? url : "https://" + url;

                CustomPlaceholder placeholder = RoseChatAPI.getInstance().getPlaceholderManager().getPlaceholder("url");
                StringPlaceholders placeholders = MessageUtils.getSenderViewerPlaceholders(messageWrapper.getSender(), viewer, messageWrapper.getGroup())
                        .addPlaceholder("message", content).build();

                content = placeholders.apply(placeholder.getText().parse(messageWrapper.getSender(), viewer, placeholders));
                String hover = placeholders.apply(placeholder.getHover().parse(messageWrapper.getSender(), viewer, placeholders));

                return new URLToken(originalContent, content, url, hover);
            }
        }

        return null;
    }

}

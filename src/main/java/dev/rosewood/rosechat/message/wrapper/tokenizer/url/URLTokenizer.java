package dev.rosewood.rosechat.message.wrapper.tokenizer.url;

import dev.rosewood.rosechat.chat.Group;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;
import java.util.regex.Matcher;

public class URLTokenizer implements Tokenizer<URLToken> {

    @Override
    public URLToken tokenize(MessageWrapper messageWrapper, Group group, RoseSender sender, RoseSender viewer, MessageLocation location, String input) {
        if (input.startsWith("[")) {
            Matcher matcher = MessageUtils.URL_MARKDOWN_PATTERN.matcher(input);
            if (matcher.find()) {
                return new URLToken(sender, viewer, group, input.substring(matcher.start(), matcher.end()), matcher.group(1), matcher.group(2));
            }
        }
        return null;
    }
}

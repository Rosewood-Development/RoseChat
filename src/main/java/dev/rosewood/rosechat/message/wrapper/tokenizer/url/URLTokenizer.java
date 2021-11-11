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
    public URLToken tokenize(MessageWrapper wrapper, Group group, RoseSender sender, RoseSender viewer, MessageLocation location, String input) {
        Matcher matcher = MessageUtils.URL_PATTERN.matcher(input);
        if (matcher.find()) {
            String found = input.substring(matcher.start(), matcher.end());
            if (input.startsWith(found)) {
                return new URLToken(group, sender, viewer, input.substring(matcher.start(), matcher.end()));
            }
        }

        return null;
    }
}

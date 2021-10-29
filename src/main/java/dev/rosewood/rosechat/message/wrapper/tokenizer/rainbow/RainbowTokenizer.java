package dev.rosewood.rosechat.message.wrapper.tokenizer.rainbow;

import dev.rosewood.rosechat.chat.Group;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.ComponentColorizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;
import java.util.regex.Matcher;

public class RainbowTokenizer implements Tokenizer<RainbowToken> {

    @Override
    public RainbowToken tokenize(MessageWrapper wrapper, Group group, RoseSender sender, RoseSender viewer, MessageLocation location, String input) {
        if (input.startsWith("<r")) {
            Matcher matcher = ComponentColorizer.RAINBOW_PATTERN.matcher(input);
            if (matcher.find()) {
                return new RainbowToken(sender, viewer, input.substring(matcher.start(), matcher.end()));
            }
        }

        return null;
    }
}

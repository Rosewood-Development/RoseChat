package dev.rosewood.rosechat.message.wrapper.tokenizer.gradient;

import dev.rosewood.rosechat.chat.Group;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.ComponentColorizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;
import java.util.regex.Matcher;

public class GradientTokenizer implements Tokenizer<GradientToken> {

    @Override
    public GradientToken tokenize(MessageWrapper wrapper, Group group, RoseSender sender, RoseSender viewer, MessageLocation location, String input) {
        if (input.startsWith("<g")) {
            Matcher matcher = ComponentColorizer.GRADIENT_PATTERN.matcher(input);
            if (matcher.find()) {
                return new GradientToken(sender, viewer, input.substring(matcher.start(), matcher.end()));
            }
        }

        return null;
    }
}
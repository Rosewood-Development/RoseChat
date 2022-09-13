package dev.rosewood.rosechat.message.wrapper.tokenizer.rainbow;

import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;
import java.util.regex.Matcher;

public class RainbowTokenizer implements Tokenizer<RainbowToken> {

    @Override
    public RainbowToken tokenize(MessageWrapper messageWrapper, RoseSender viewer, String input, boolean ignorePermissions) {
        // Check if the content contains the rainbow pattern.
        Matcher matcher = MessageUtils.RAINBOW_PATTERN.matcher(input);
        if (matcher.find() && matcher.start() == 0) {
            // Retrieve parameters from the rainbow pattern.
            float saturation = 1.0F;
            float brightness = 1.0F;

            String saturationGroup = MessageUtils.getCaptureGroup(matcher, "saturation");
            if (saturationGroup != null) {
                try {
                    saturation = Float.parseFloat(saturationGroup);
                } catch (NumberFormatException ignored) { }
            }

            String brightnessGroup = MessageUtils.getCaptureGroup(matcher, "brightness");
            if (brightnessGroup != null) {
                try {
                    brightness = Float.parseFloat(brightnessGroup);
                } catch (NumberFormatException ignored) { }
            }

            return hasPermission(messageWrapper, ignorePermissions, "rosechat.rainbow") ?
                    new RainbowToken(input.substring(matcher.start(), matcher.end()), saturation, brightness) : new RainbowToken(input.substring(matcher.start(), matcher.end()), 0.0f, 0.0f);
        }

        return null;
    }

}

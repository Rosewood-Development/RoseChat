package dev.rosewood.rosechat.message.wrapper.tokenizer.rainbow;

import dev.rosewood.rosechat.message.MessageWrapper;
import static dev.rosewood.rosechat.message.wrapper.ComponentColorizer.*;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;
import java.util.regex.Matcher;

public class RainbowTokenizer implements Tokenizer<RainbowToken> {

    @Override
    public RainbowToken tokenize(MessageWrapper messageWrapper, String input) {
        // Check if the content contains the rainbow pattern.
        Matcher matcher = RAINBOW_PATTERN.matcher(input);
        if (matcher.find() && matcher.start() == 0) {
            // Retrieve parameters from the rainbow pattern.
            float saturation = 1.0F;
            float brightness = 1.0F;

            String saturationGroup = getCaptureGroup(matcher, "saturation");
            if (saturationGroup != null) {
                try {
                    saturation = Float.parseFloat(saturationGroup);
                } catch (NumberFormatException ignored) { }
            }

            String brightnessGroup = getCaptureGroup(matcher, "brightness");
            if (brightnessGroup != null) {
                try {
                    brightness = Float.parseFloat(brightnessGroup);
                } catch (NumberFormatException ignored) { }
            }

            return new RainbowToken(input.substring(matcher.start(), matcher.end()), saturation, brightness);
        }

        return null;
    }

}

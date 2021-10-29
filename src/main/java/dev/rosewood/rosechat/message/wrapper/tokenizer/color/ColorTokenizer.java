package dev.rosewood.rosechat.message.wrapper.tokenizer.color;

import dev.rosewood.rosechat.chat.Group;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.ComponentColorizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;
import java.util.regex.Matcher;

public class ColorTokenizer implements Tokenizer<ColorToken> {

    @Override
    public ColorToken tokenize(MessageWrapper messageWrapper, Group group, RoseSender sender, RoseSender viewer, MessageLocation location, String input) {
        Matcher legacyMatcher = ComponentColorizer.VALID_LEGACY_REGEX.matcher(input);
        Matcher legacyFormattingMatcher = ComponentColorizer.VALID_LEGACY_REGEX_FORMATTING.matcher(input);
        Matcher hexMatcher = ComponentColorizer.HEX_REGEX.matcher(input);
        if (legacyMatcher.find()) {
            String match = input.substring(legacyMatcher.start(), legacyMatcher.end());
            if (input.startsWith(match)) {
                return new ColorToken(sender, viewer, input.substring(legacyMatcher.start(), legacyMatcher.end()));
            }
        }

        if (legacyFormattingMatcher.find()) {
            String match = input.substring(legacyFormattingMatcher.start(), legacyFormattingMatcher.end());
            if (input.startsWith(match)) {
                return new ColorToken(sender, viewer, input.substring(legacyFormattingMatcher.start(), legacyFormattingMatcher.end()));
            }
        }

        if (hexMatcher.find()) {
            String match = input.substring(hexMatcher.start(), hexMatcher.end());
            if (input.startsWith(match)) {
                return new ColorToken(sender, viewer, input.substring(hexMatcher.start(), hexMatcher.end()));
            }
        }

        return null;
    }
}

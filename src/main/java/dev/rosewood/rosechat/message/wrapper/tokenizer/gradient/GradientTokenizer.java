package dev.rosewood.rosechat.message.wrapper.tokenizer.gradient;

import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.wrapper.ComponentColorizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;
import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class GradientTokenizer implements Tokenizer<GradientToken> {

    @Override
    public GradientToken tokenize(MessageWrapper messageWrapper, String input) {
        Matcher matcher = ComponentColorizer.GRADIENT_PATTERN.matcher(input);
        if (matcher.find() && matcher.start() == 0) {
            List<Color> hexSteps = Arrays.stream(ComponentColorizer.getCaptureGroup(matcher, "hex").substring(1).split(":"))
                    .map(x -> x.length() != 4 ? x : String.format("#%s%s%s%s%s%s", x.charAt(1), x.charAt(1), x.charAt(2), x.charAt(2), x.charAt(3), x.charAt(3)))
                    .map(Color::decode)
                    .collect(Collectors.toList());
            return new GradientToken(input.substring(matcher.start(), matcher.end()), hexSteps);
        }

        return null;
    }
}

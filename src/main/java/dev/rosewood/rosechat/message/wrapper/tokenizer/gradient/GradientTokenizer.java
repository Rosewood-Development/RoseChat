package dev.rosewood.rosechat.message.wrapper.tokenizer.gradient;

import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;
import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class GradientTokenizer implements Tokenizer<GradientToken> {

    @Override
    public GradientToken tokenize(MessageWrapper messageWrapper, RoseSender viewer, String input, boolean ignorePermissions) {
        Matcher matcher = MessageUtils.GRADIENT_PATTERN.matcher(input);
        if (matcher.find()) {
            if (matcher.start() != 0) return null;
            List<Color> hexSteps = Arrays.stream(MessageUtils.getCaptureGroup(matcher, "hex").substring(1).split(":"))
                    .map(x -> x.length() != 4 ? x : String.format("#%s%s%s%s%s%s", x.charAt(1), x.charAt(1), x.charAt(2), x.charAt(2), x.charAt(3), x.charAt(3)))
                    .map(Color::decode)
                    .collect(Collectors.toList());
            String content = input.substring(matcher.start(), matcher.end());
            return this.hasPermission(messageWrapper, ignorePermissions || MessageUtils.hasDefaultColor(input, messageWrapper), "rosechat.gradient") ?
                    new GradientToken(content, hexSteps) : new GradientToken(content, null);
        }

        return null;
    }

}

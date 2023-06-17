package dev.rosewood.rosechat.message.tokenizer.gradient;

import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class GradientTokenizer implements Tokenizer<GradientToken> {

    @Override
    public GradientToken tokenize(RoseMessage roseMessage, RosePlayer viewer, String input, boolean ignorePermissions) {
        if (!input.startsWith("<"))
            return null;

        Matcher matcher = MessageUtils.GRADIENT_PATTERN.matcher(input);
        if (matcher.find()) {
            if (matcher.start() != 0) return null;
            List<Color> hexSteps = Arrays.stream(MessageUtils.getCaptureGroup(matcher, "hex").substring(1).split(":"))
                    .map(x -> x.length() != 4 ? x : String.format("#%s%s%s%s%s%s", x.charAt(1), x.charAt(1), x.charAt(2), x.charAt(2), x.charAt(3), x.charAt(3)))
                    .map(Color::decode)
                    .collect(Collectors.toList());

            int speed = 0;
            String speedGroup = matcher.group("speed");
            if (speedGroup != null) {
                try {
                    speed = Integer.parseInt(speedGroup);
                } catch (NumberFormatException ignored) {}
            }

            String content = input.substring(matcher.start(), matcher.end());
            return ignorePermissions || MessageUtils.hasDefaultColor(input, roseMessage) || MessageUtils.hasTokenPermission(roseMessage, "rosechat.gradient") ?
                    new GradientToken(content, hexSteps, speed) : new GradientToken(content, null, speed);
        }

        return null;
    }

}

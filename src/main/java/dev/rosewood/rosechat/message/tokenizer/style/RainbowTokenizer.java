package dev.rosewood.rosechat.message.tokenizer.style;

import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import dev.rosewood.rosechat.message.tokenizer.decorator.ColorDecorator;
import dev.rosewood.rosegarden.utils.HexUtils;
import java.util.function.Function;
import java.util.regex.Matcher;

public class RainbowTokenizer implements Tokenizer {

    @Override
    public TokenizerResult tokenize(TokenizerParams params) {
        String input = params.getInput();
        if (!input.startsWith("<"))
            return null;

        // Check if the content contains the rainbow pattern.
        Matcher matcher = MessageUtils.RAINBOW_PATTERN.matcher(input);
        if (!matcher.find())
            return null;

        if (matcher.start() != 0) return null;

        var rainbowValues = new Object() {
            int speed = 0;
            float saturation = 1.0F;
            float brightness = 1.0F;
        };

        // Retrieve parameters from the rainbow pattern.
        String saturationGroup = MessageUtils.getCaptureGroup(matcher, "saturation");
        if (saturationGroup != null)
            rainbowValues.saturation = Float.parseFloat(saturationGroup);

        String brightnessGroup = MessageUtils.getCaptureGroup(matcher, "brightness");
        if (brightnessGroup != null)
            rainbowValues.brightness = Float.parseFloat(brightnessGroup);

        String speedGroup = matcher.group("speed");
        if (speedGroup != null)
            rainbowValues.speed = Integer.parseInt(speedGroup);

        Function<Integer, HexUtils.ColorGenerator> generatorGenerator = contentLength -> {
            HexUtils.ColorGenerator generator;
            if (rainbowValues.speed > 0) {
                generator = new HexUtils.AnimatedRainbow(contentLength, rainbowValues.saturation, rainbowValues.brightness, rainbowValues.speed);
            } else {
                generator = new HexUtils.Rainbow(contentLength, rainbowValues.saturation, rainbowValues.brightness);
            }
            return generator;
        };

        String content = matcher.group();
        return MessageUtils.hasTokenPermission(params, "rosechat.rainbow")
                ? new TokenizerResult(Token.decorator().decorate(ColorDecorator.of(generatorGenerator)).build(), content.length())
                : new TokenizerResult(Token.text(content).build(), content.length());
    }

}

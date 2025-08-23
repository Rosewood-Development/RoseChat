package dev.rosewood.rosechat.message.tokenizer.style;

import dev.rosewood.rosechat.config.Settings;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import dev.rosewood.rosechat.message.tokenizer.decorator.ColorDecorator;
import dev.rosewood.rosechat.message.tokenizer.decorator.ShadowColorDecorator;
import dev.rosewood.rosegarden.utils.HexUtils;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;

public class RainbowTokenizer extends Tokenizer {

    public RainbowTokenizer() {
        super("rainbow");
    }

    @Override
    public List<TokenizerResult> tokenize(TokenizerParams params) {
        String rawInput = params.getInput();
        String input = rawInput.charAt(0) == MessageUtils.ESCAPE_CHAR ? rawInput.substring(1) : rawInput;
        if (rawInput.charAt(0) == MessageUtils.ESCAPE_CHAR && !params.getSender().hasPermission("rosechat.escape"))
            return null;

        boolean shadow;
        if (ShadowColorDecorator.VALID_VERSION && input.charAt(0) == MessageUtils.SHADOW_PREFIX && input.length() >= 3) {
            input = input.substring(1);
            shadow = true;
        } else shadow = false;

        if (!input.startsWith("<"))
            return null;

        // Check if the content contains the rainbow pattern.
        Matcher matcher = MessageUtils.RAINBOW_PATTERN.matcher(input);
        if (!matcher.find() || matcher.start() != 0)
            return null;

        var rainbowValues = new Object() {
            int speed = 0;
            float saturation = 1.0F;
            float brightness = 1.0F;
        };

        // Retrieve parameters from the rainbow pattern.
        String saturationGroup = this.getCaptureGroup(matcher, "saturation");
        if (saturationGroup != null)
            rainbowValues.saturation = Float.parseFloat(saturationGroup);

        String brightnessGroup = this.getCaptureGroup(matcher, "brightness");
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
        if (rawInput.charAt(0) == MessageUtils.ESCAPE_CHAR) {
            if (shadow)
                content = MessageUtils.SHADOW_PREFIX + content;
            return List.of(new TokenizerResult(Token.text(content), content.length() + (shadow ? 2 : 1)));
        }

        return this.hasTokenPermission(params, "rosechat." + (shadow ? "shadow." : "") + "rainbow")
                ? List.of(new TokenizerResult(Token.decorator(!shadow ? new ColorDecorator(generatorGenerator) : new ShadowColorDecorator(generatorGenerator)), content.length() + (shadow ? 1 : 0)))
                : List.of(new TokenizerResult(Token.text(Settings.REMOVE_COLOR_CODES.get() ? "" : (shadow ? MessageUtils.SHADOW_PREFIX : "") + content), content.length() + (shadow ? 1 : 0)));
    }

}

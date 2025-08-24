package dev.rosewood.rosechat.message.tokenizer.style;

import dev.rosewood.rosechat.config.Settings;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import dev.rosewood.rosechat.message.tokenizer.decorator.ColorDecorator;
import dev.rosewood.rosechat.message.tokenizer.decorator.ShadowColorDecorator;
import dev.rosewood.rosechat.message.tokenizer.decorator.TokenDecorator;
import dev.rosewood.rosegarden.utils.HexUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;

public class RainbowTokenizer extends Tokenizer {

    public RainbowTokenizer() {
        super("rainbow");
    }

    @Override
    public List<TokenizerResult> tokenize(TokenizerParams params) {
        String input = params.getInput();
        Matcher matcher = MessageUtils.RAINBOW_PATTERN.matcher(input);

        List<TokenizerResult> results = new ArrayList<>();

        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            String match = matcher.group();

            boolean shadow = ShadowColorDecorator.VALID_VERSION && start > 0 && input.charAt(start - 1) == MessageUtils.SHADOW_PREFIX;
            boolean escape = (start > (shadow ? 1 : 0)) && input.charAt(start - (shadow ? 2 : 1)) == MessageUtils.ESCAPE_CHAR && params.getSender().hasPermission("rosechat.escape");

            int offset = (shadow ? 1 : 0) + (escape ? 1 : 0);
            int realStart = start - offset;
            int consumed = match.length() + offset;
            if (escape) {
                String rawInput = input.substring(realStart + 1, end);
                results.add(new TokenizerResult(Token.text(rawInput), realStart, consumed));
                continue;
            }

            if (!this.hasTokenPermission(params, "rosechat." + (shadow ? "shadow." : "") + "rainbow")) {
                results.add(new TokenizerResult(Token.text(Settings.REMOVE_COLOR_CODES.get() ? "" : (shadow ? MessageUtils.SHADOW_PREFIX : "") + match), realStart, consumed));
                continue;
            }

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

            TokenDecorator decorator = shadow ? new ShadowColorDecorator(generatorGenerator) : new ColorDecorator(generatorGenerator);
            results.add(new TokenizerResult(Token.decorator(decorator), realStart, consumed));
        }

        return results;
    }

}

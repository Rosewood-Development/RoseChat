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
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;

public class GradientTokenizer extends Tokenizer {

    public GradientTokenizer() {
        super("gradient");
    }

    @Override
    public List<TokenizerResult> tokenize(TokenizerParams params) {
        String input = params.getInput();
        Matcher matcher = MessageUtils.GRADIENT_PATTERN.matcher(input);

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

            if (!this.hasTokenPermission(params, "rosechat." + (shadow ? "shadow." : "") + "gradient")) {
                results.add(new TokenizerResult(Token.text(Settings.REMOVE_COLOR_CODES.get() ? "" : (shadow ? MessageUtils.SHADOW_PREFIX : "") + match), realStart, consumed));
                continue;
            }

            List<Color> hexSteps = Arrays.stream(this.getCaptureGroup(matcher, "hex").substring(1).split(":"))
                    .map(x -> x.length() != 4 ? x : String.format("#%s%s%s%s%s%s", x.charAt(1), x.charAt(1), x.charAt(2), x.charAt(2), x.charAt(3), x.charAt(3)))
                    .map(Color::decode)
                    .toList();

            int speed = 0;
            String speedGroup = matcher.group("speed");
            if (speedGroup != null) {
                try {
                    speed = Integer.parseInt(speedGroup);
                } catch (NumberFormatException ignored) {}
            }

            int finalSpeed = speed;
            Function<Integer, HexUtils.ColorGenerator> generatorGenerator = contentLength -> {
                HexUtils.ColorGenerator generator;
                if (finalSpeed > 0) {
                    generator = new HexUtils.AnimatedGradient(hexSteps, contentLength, finalSpeed);
                } else {
                    generator = new HexUtils.Gradient(hexSteps, contentLength);
                }

                return generator;
            };

            TokenDecorator decorator = shadow ? new ShadowColorDecorator(generatorGenerator) : new ColorDecorator(generatorGenerator);
            results.add(new TokenizerResult(Token.decorator(decorator), realStart, consumed));
        }

        return results;
    }

}

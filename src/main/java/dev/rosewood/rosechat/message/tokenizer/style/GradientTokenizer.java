package dev.rosewood.rosechat.message.tokenizer.style;

import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import dev.rosewood.rosechat.message.tokenizer.decorator.ColorDecorator;
import dev.rosewood.rosegarden.utils.HexUtils;
import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;

public class GradientTokenizer extends Tokenizer {

    public GradientTokenizer() {
        super("gradient");
    }

    @Override
    public TokenizerResult tokenize(TokenizerParams params) {
        String input = params.getInput();
        if (!input.startsWith("<")) return null;

        Matcher matcher = MessageUtils.GRADIENT_PATTERN.matcher(input);
        if (matcher.find()) {
            if (matcher.start() != 0) return null;
            List<Color> hexSteps = Arrays.stream(MessageUtils.getCaptureGroup(matcher, "hex").substring(1).split(":"))
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
                    generator = new HexUtils.AnimatedGradient(hexSteps, finalSpeed, contentLength);
                } else {
                    generator = new HexUtils.Gradient(hexSteps, contentLength);
                }
                return generator;
            };

            String content = matcher.group();
            return MessageUtils.hasTokenPermission(params, "rosechat.gradient")
                    ? new TokenizerResult(Token.decorator(ColorDecorator.of(generatorGenerator)).build(), content.length())
                    : new TokenizerResult(Token.text(content).build(), content.length());
        }

        return null;
    }

}

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
    public List<TokenizerResult> tokenize(TokenizerParams params) {
        if (true) return null;
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

        Matcher matcher = MessageUtils.GRADIENT_PATTERN.matcher(input);
        if (!matcher.find() || matcher.start() != 0)
            return null;

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
                generator = new HexUtils.AnimatedGradient(hexSteps, finalSpeed, contentLength);
            } else {
                generator = new HexUtils.Gradient(hexSteps, contentLength);
            }

            return generator;
        };

        String content = matcher.group();
        if (rawInput.charAt(0) == MessageUtils.ESCAPE_CHAR) {
            if (shadow)
                content = MessageUtils.SHADOW_PREFIX + content;
            return List.of(new TokenizerResult(Token.text(content), content.length() + (shadow ? 2 : 1)));
        }

        return this.hasTokenPermission(params, "rosechat." + (shadow ? "shadow." : "") + "gradient")
                ? List.of(new TokenizerResult(Token.decorator(!shadow ? new ColorDecorator(generatorGenerator) : new ShadowColorDecorator(generatorGenerator)), content.length() + (shadow ? 1 : 0)))
                : List.of(new TokenizerResult(Token.text(Settings.REMOVE_COLOR_CODES.get() ? "" : (shadow ? MessageUtils.SHADOW_PREFIX : "") + content), content.length() + (shadow ? 1 : 0)));
    }

}

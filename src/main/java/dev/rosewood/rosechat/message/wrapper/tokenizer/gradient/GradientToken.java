package dev.rosewood.rosechat.message.wrapper.tokenizer.gradient;

import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import dev.rosewood.rosegarden.utils.HexUtils;
import java.awt.Color;
import java.util.List;

public class GradientToken extends Token {

    private final List<Color> colors;

    public GradientToken(String originalText, List<Color> colors) {
        super(new TokenSettings(originalText).content(""));

        this.colors = colors;
    }

    @Override
    public HexUtils.ColorGenerator getColorGenerator(List<Token> futureTokens) {
        return new HexUtils.Gradient(this.colors, this.getColorGeneratorContentLength(futureTokens));
    }

    @Override
    public boolean hasColorGenerator() {
        return colors != null;
    }

}

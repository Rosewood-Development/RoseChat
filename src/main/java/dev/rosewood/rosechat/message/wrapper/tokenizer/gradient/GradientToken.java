package dev.rosewood.rosechat.message.wrapper.tokenizer.gradient;

import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import dev.rosewood.rosegarden.utils.HexUtils;
import java.awt.Color;
import java.util.List;

public class GradientToken extends Token {

    private final List<Color> colors;

    public GradientToken(String originalText, List<Color> colors) {
        super(originalText);

        this.colors = colors;
    }

    @Override
    public String getText(MessageWrapper wrapper) {
        return "";
    }

    @Override
    public HexUtils.ColorGenerator getColorGenerator(MessageWrapper wrapper, List<Token> futureTokens) {
        int contentLength = 0;
        for (Token token : futureTokens) {
            if (!token.hasColorGenerator() || token == this) {
                contentLength += token.getText(wrapper).length();
            } else break;
        }

        return new HexUtils.Gradient(this.colors, contentLength);
    }

    @Override
    public boolean hasColorGenerator() {
        return true;
    }

}

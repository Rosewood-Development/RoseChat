package dev.rosewood.rosechat.message.wrapper.tokenizer.gradient;

import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
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
    public String getContent(MessageWrapper wrapper, RoseSender viewer) {
        return "";
    }

    @Override
    public HexUtils.ColorGenerator getColorGenerator(MessageWrapper wrapper, RoseSender viewer, List<Token> futureTokens) {
        return new HexUtils.Gradient(this.colors, this.getColorGeneratorContentLength(wrapper, viewer, futureTokens));
    }

    @Override
    public boolean hasColorGenerator() {
        return true;
    }

}

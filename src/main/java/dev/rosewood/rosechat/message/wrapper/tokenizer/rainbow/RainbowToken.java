package dev.rosewood.rosechat.message.wrapper.tokenizer.rainbow;

import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.wrapper.ComponentColorizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;

import java.util.List;

public class RainbowToken extends Token {

    private final float saturation, brightness;

    public RainbowToken(String originalText, float saturation, float brightness) {
        super(originalText);

        this.saturation = saturation;
        this.brightness = brightness;
    }

    @Override
    public String getText(MessageWrapper wrapper) {
        return "";
    }

    @Override
    public ComponentColorizer.ColorGenerator getColorGenerator(MessageWrapper wrapper, List<Token> futureTokens) {
        int contentLength = 0;
        for (Token token : futureTokens) {
            if (!token.hasColorGenerator() || token == this) {
                contentLength += token.getText(wrapper).length();
            } else break;
        }

        return new ComponentColorizer.Rainbow(contentLength, this.saturation, this.brightness);
    }

    @Override
    public boolean hasColorGenerator() {
        return true;
    }

}

package dev.rosewood.rosechat.message.wrapper.tokenizer.rainbow;

import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import dev.rosewood.rosegarden.utils.HexUtils;
import java.util.List;

public class RainbowToken extends Token {

    private final float saturation, brightness;

    public RainbowToken(String originalText, float saturation, float brightness) {
        super(new TokenSettings(originalText).content(""));

        this.saturation = saturation;
        this.brightness = brightness;
    }

    @Override
    public HexUtils.ColorGenerator getColorGenerator(List<Token> futureTokens) {
        return new HexUtils.Rainbow(this.getColorGeneratorContentLength(futureTokens), this.saturation, this.brightness);
    }

    @Override
    public boolean hasColorGenerator() {
        return true;
    }

}

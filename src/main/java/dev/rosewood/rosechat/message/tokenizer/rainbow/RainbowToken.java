package dev.rosewood.rosechat.message.tokenizer.rainbow;

import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosegarden.utils.HexUtils;
import java.util.List;

public class RainbowToken extends Token {

    private final float saturation, brightness;
    private final int speed;

    public RainbowToken(String originalText, float saturation, float brightness, int speed) {
        super(new TokenSettings(originalText).content(""));

        this.saturation = saturation;
        this.brightness = brightness;
        this.speed = speed;
    }

    @Override
    public HexUtils.ColorGenerator getColorGenerator(List<Token> futureTokens) {
        return this.speed == 0 ? new HexUtils.Rainbow(this.getColorGeneratorContentLength(futureTokens), this.saturation, this.brightness)
                : new HexUtils.AnimatedRainbow(this.getColorGeneratorContentLength(futureTokens), this.saturation, this.brightness, this.speed);
    }

    @Override
    public boolean hasColorGenerator() {
        return this.saturation != 0.0f && this.brightness != 0.0f;
    }

}

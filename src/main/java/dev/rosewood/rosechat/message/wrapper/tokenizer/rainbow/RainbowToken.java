package dev.rosewood.rosechat.message.wrapper.tokenizer.rainbow;

import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import dev.rosewood.rosegarden.utils.HexUtils;
import java.util.List;

public class RainbowToken extends Token {

    private final float saturation, brightness;

    public RainbowToken(String originalText, float saturation, float brightness) {
        super(originalText);

        this.saturation = saturation;
        this.brightness = brightness;
    }

    @Override
    public String getContent(MessageWrapper wrapper, RoseSender viewer) {
        return "";
    }

    @Override
    public HexUtils.ColorGenerator getColorGenerator(MessageWrapper wrapper, RoseSender viewer, List<Token> futureTokens) {
        return new HexUtils.Rainbow(this.getColorGeneratorContentLength(wrapper, viewer, futureTokens), this.saturation, this.brightness);
    }

    @Override
    public boolean hasColorGenerator() {
        return true;
    }

}

//package dev.rosewood.rosechat.message.tokenizer.gradient;
//
//import dev.rosewood.rosechat.message.tokenizer.Token;
//import dev.rosewood.rosegarden.utils.HexUtils;
//import java.awt.Color;
//import java.util.List;
//
//public class GradientToken extends Token {
//
//    private final List<Color> colors;
//    private final int speed;
//
//    public GradientToken(String originalText, List<Color> colors, int speed) {
//        super(new TokenSettings(originalText).content("").requiresTokenizing(false));
//
//        this.colors = colors;
//        this.speed = speed;
//    }
//
//    @Override
//    public HexUtils.ColorGenerator getColorGenerator(List<Token> futureTokens) {
//        return this.speed == 0 ? new HexUtils.Gradient(this.colors, this.getColorGeneratorContentLength(futureTokens))
//                : new HexUtils.AnimatedGradient(this.colors, this.getColorGeneratorContentLength(futureTokens), 1);
//    }
//
//    @Override
//    public boolean hasColorGenerator() {
//        return true;
//    }
//
//}

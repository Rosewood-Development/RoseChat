package dev.rosewood.rosechat.message.wrapper.tokenizer.color;

import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import dev.rosewood.rosegarden.utils.HexUtils;
import net.md_5.bungee.api.ChatColor;
import java.awt.Color;
import java.util.List;

public class ColorToken extends Token {

    private final ChatColor color;

    public ColorToken(String originalText, ChatColor color) {
        super(new TokenSettings(originalText).content(""));

        this.color = color;
    }

    @Override
    public HexUtils.ColorGenerator getColorGenerator(List<Token> futureTokens) {
        return new SolidColor(this.color);
    }

    @Override
    public boolean hasColorGenerator() {
        return true;
    }

    public static class SolidColor implements HexUtils.ColorGenerator {

        private final ChatColor color;

        public SolidColor(ChatColor color) {
            this.color = color;
        }

        @Override
        public ChatColor nextChatColor() {
            return this.color;
        }

        @Override
        public Color nextColor() {
            return this.color.getColor();
        }

    }
    
}

package dev.rosewood.rosechat.message.wrapper.tokenizer.color;

import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.wrapper.ComponentColorizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import net.md_5.bungee.api.ChatColor;

import java.util.List;

public class ColorToken extends Token {

    private final ChatColor color;

    public ColorToken(String originalText, ChatColor color) {
        super(originalText);

        this.color = color;
    }

    @Override
    public String getText(MessageWrapper wrapper) {
        return "";
    }

    @Override
    public ComponentColorizer.ColorGenerator getColorGenerator(MessageWrapper wrapper, List<Token> futureTokens) {
        return new ComponentColorizer.SolidColor(this.color);
    }

    @Override
    public boolean hasColorGenerator() {
        return true;
    }

}

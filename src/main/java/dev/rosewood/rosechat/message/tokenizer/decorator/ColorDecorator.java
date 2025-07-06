package dev.rosewood.rosechat.message.tokenizer.decorator;

import dev.rosewood.rosegarden.utils.HexUtils;
import java.awt.Color;
import java.util.function.Function;
import net.md_5.bungee.api.ChatColor;

public abstract class ColorDecorator implements TokenDecorator {

    protected final Function<Integer, HexUtils.ColorGenerator> colorGeneratorFunction;
    protected HexUtils.ColorGenerator colorGenerator;

    protected ColorDecorator(Function<Integer, HexUtils.ColorGenerator> colorGeneratorFunction) {
        this.colorGeneratorFunction = colorGeneratorFunction;
        this.colorGenerator = null;
    }

    protected ColorDecorator(ChatColor chatColor) {
        this.colorGeneratorFunction = null;
        this.colorGenerator = new SolidColorGenerator(chatColor);
    }

    @Override
    public DecoratorType getType() {
        return DecoratorType.STYLING;
    }

    @Override
    public boolean isOverwrittenBy(TokenDecorator newDecorator) {
        if (newDecorator instanceof FormatDecorator formatDecorator)
            return formatDecorator.formatType == FormatDecorator.FormatType.RESET;

        return TokenDecorator.super.isOverwrittenBy(newDecorator);
    }

    @Override
    public boolean blocksTextStitching() {
        return !(this.colorGenerator instanceof SolidColorGenerator);
    }

    protected record SolidColorGenerator(ChatColor chatColor) implements HexUtils.ColorGenerator {

        @Override
        public ChatColor nextChatColor() {
            return this.chatColor;
        }

        @Override
        public Color nextColor() {
            return this.chatColor.getColor();
        }

    }

}

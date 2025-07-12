package dev.rosewood.rosechat.message.tokenizer.decorator;

import dev.rosewood.rosegarden.utils.HexUtils;
import java.awt.Color;
import java.util.function.Function;
import net.md_5.bungee.api.ChatColor;

public record ColorDecorator(Function<Integer, HexUtils.ColorGenerator> colorGeneratorFunction, boolean solid) implements TokenDecorator {

    public ColorDecorator(Function<Integer, HexUtils.ColorGenerator> colorGeneratorFunction) {
        this(colorGeneratorFunction, false);
    }

    public ColorDecorator(ChatColor chatColor) {
        this(x -> new SolidColorGenerator(chatColor), true);
    }

    @Override
    public DecoratorType getType() {
        return DecoratorType.STYLING;
    }

    @Override
    public boolean isOverwrittenBy(TokenDecorator newDecorator) {
        if (newDecorator.getRoot() instanceof FormatDecorator formatDecorator)
            return formatDecorator.formatType() == FormatDecorator.FormatType.RESET;

        return TokenDecorator.super.isOverwrittenBy(newDecorator);
    }

    @Override
    public boolean blocksTextStitching() {
        return !this.solid;
    }

    private record SolidColorGenerator(ChatColor chatColor) implements HexUtils.ColorGenerator {

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

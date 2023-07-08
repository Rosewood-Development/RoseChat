package dev.rosewood.rosechat.message.tokenizer.decorator;

import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import java.awt.Color;
import java.util.function.Function;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;

public class ColorDecorator extends TokenDecorator {

    private final Function<Integer, HexUtils.ColorGenerator> colorGeneratorFunction;
    private HexUtils.ColorGenerator colorGenerator;

    private ColorDecorator(Function<Integer, HexUtils.ColorGenerator> colorGeneratorFunction) {
        super(DecoratorType.STYLING);
        this.colorGeneratorFunction = colorGeneratorFunction;
        this.colorGenerator = null;
    }

    private ColorDecorator(HexUtils.ColorGenerator colorGenerator) {
        super(DecoratorType.STYLING);
        this.colorGeneratorFunction = null;
        this.colorGenerator = colorGenerator;
    }

    @Override
    public void apply(BaseComponent component, MessageTokenizer tokenizer, StringPlaceholders placeholders) {
        if (this.colorGenerator == null && this.colorGeneratorFunction != null)
            this.colorGenerator = this.colorGeneratorFunction.apply(tokenizer.findDecoratorContentLength(this));

        if (component.toPlainText().replaceAll("\\s", "").isEmpty())
            return;

        if (this.colorGenerator != null)
            component.setColor(this.colorGenerator.nextChatColor());
    }

    @Override
    public boolean isOverwrittenBy(TokenDecorator newDecorator) {
        if (newDecorator instanceof FormatDecorator formatDecorator)
            return formatDecorator.chatColor == ChatColor.RESET;
        return super.isOverwrittenBy(newDecorator);
    }

    @Override
    public boolean blocksTextStitching() {
        return !(this.colorGenerator instanceof SolidColorGenerator);
    }

    public static ColorDecorator of(ChatColor chatColor) {
        return new ColorDecorator(new SolidColorGenerator(chatColor));
    }

    public static ColorDecorator of(Function<Integer, HexUtils.ColorGenerator> colorGeneratorFunction) {
        return new ColorDecorator(colorGeneratorFunction);
    }

    private static class SolidColorGenerator implements HexUtils.ColorGenerator {

        private final ChatColor chatColor;

        public SolidColorGenerator(ChatColor chatColor) {
            this.chatColor = chatColor;
        }

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

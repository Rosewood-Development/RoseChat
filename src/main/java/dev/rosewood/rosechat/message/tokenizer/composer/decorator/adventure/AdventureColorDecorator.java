package dev.rosewood.rosechat.message.tokenizer.composer.decorator.adventure;

import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.decorator.ColorDecorator;
import dev.rosewood.rosegarden.utils.HexUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;

public class AdventureColorDecorator extends AdventureTokenDecorator<ColorDecorator> {

    private HexUtils.ColorGenerator colorGenerator;

    public AdventureColorDecorator(ColorDecorator decorator) {
        super(decorator);
    }

    @Override
    public Component apply(Component component, Token parent) {
        if (this.colorGenerator == null && this.decorator.colorGeneratorFunction() != null)
            this.colorGenerator = this.decorator.colorGeneratorFunction().apply(MessageTokenizer.findDecoratorContentLength(parent, this));

        if (this.colorGenerator != null && (!(component instanceof TextComponent textComponent) || !textComponent.content().isBlank()))
            return component.color(TextColor.color(this.colorGenerator.nextChatColor().getColor().getRGB()));

        return component;
    }

}

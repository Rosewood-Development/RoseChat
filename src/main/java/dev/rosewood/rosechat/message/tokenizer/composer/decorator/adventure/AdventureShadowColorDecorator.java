package dev.rosewood.rosechat.message.tokenizer.composer.decorator.adventure;

import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.decorator.ShadowColorDecorator;
import dev.rosewood.rosegarden.utils.HexUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.ShadowColor;

public class AdventureShadowColorDecorator extends AdventureTokenDecorator<ShadowColorDecorator> {

    private HexUtils.ColorGenerator colorGenerator;

    public AdventureShadowColorDecorator(ShadowColorDecorator decorator) {
        super(decorator);
    }

    @Override
    public Component apply(Component component, Token parent) {
        if (this.colorGenerator == null && this.decorator.colorGeneratorFunction() != null)
            this.colorGenerator = this.decorator.colorGeneratorFunction().apply(MessageTokenizer.findDecoratorContentLength(parent, this));

        if (!this.decorator.isMarker() && this.colorGenerator != null && (!(component instanceof TextComponent textComponent) || !textComponent.content().isBlank()))
            return component.shadowColor(ShadowColor.shadowColor(this.colorGenerator.nextChatColor().getColor().getRGB()));

        return component;
    }

}

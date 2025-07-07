package dev.rosewood.rosechat.message.tokenizer.composer.decorator.adventure;

import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.decorator.FontDecorator;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;

public class AdventureFontDecorator extends AdventureTokenDecorator<FontDecorator> {

    public AdventureFontDecorator(FontDecorator decorator) {
        super(decorator);
    }

    @Override
    public Component apply(Component component, Token parent) {
        if (this.decorator.font() != null)
            return component.font(Key.key(this.decorator.font()));

        return component;
    }

}

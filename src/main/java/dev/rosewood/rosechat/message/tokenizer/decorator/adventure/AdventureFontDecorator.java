package dev.rosewood.rosechat.message.tokenizer.decorator.adventure;

import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.decorator.FontDecorator;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;

public class AdventureFontDecorator extends FontDecorator implements AdventureTokenDecorator {

    public AdventureFontDecorator(String font) {
        super(font);
    }

    @Override
    public Component apply(Component component, MessageTokenizer tokenizer, Token parent) {
        if (this.font != null)
            return component.font(Key.key(this.font));

        return component;
    }

}

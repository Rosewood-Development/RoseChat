package dev.rosewood.rosechat.message.tokenizer.decorator;

import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.chat.BaseComponent;

public class FontDecorator extends TokenDecorator {

    private final String font;

    private FontDecorator(String font) {
        super(DecoratorType.STYLING);
        this.font = font;
    }

    @Override
    public void apply(BaseComponent component, MessageTokenizer tokenizer, StringPlaceholders placeholders) {
        component.setFont(this.font);
    }

    public static FontDecorator of(String font) {
        return new FontDecorator(font);
    }

}

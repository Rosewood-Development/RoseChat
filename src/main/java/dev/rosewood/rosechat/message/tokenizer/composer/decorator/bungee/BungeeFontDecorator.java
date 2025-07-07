package dev.rosewood.rosechat.message.tokenizer.composer.decorator.bungee;

import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.decorator.FontDecorator;
import net.md_5.bungee.api.chat.BaseComponent;

public class BungeeFontDecorator extends BungeeTokenDecorator<FontDecorator> {

    public BungeeFontDecorator(FontDecorator decorator) {
        super(decorator);
    }

    @Override
    public void apply(BaseComponent component, Token parent) {
        if (this.decorator.font() != null)
            component.setFont(this.decorator.font());
    }

}

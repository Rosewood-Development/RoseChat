package dev.rosewood.rosechat.message.tokenizer.decorator.bungee;

import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.decorator.FontDecorator;
import net.md_5.bungee.api.chat.BaseComponent;

public class BungeeFontDecorator extends FontDecorator implements BungeeTokenDecorator {

    public BungeeFontDecorator(String font) {
        super(font);
    }

    @Override
    public void apply(BaseComponent component, MessageTokenizer tokenizer, Token parent) {
        if (this.font != null)
            component.setFont(this.font);
    }

}

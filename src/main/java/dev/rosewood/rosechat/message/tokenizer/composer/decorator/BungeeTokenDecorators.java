package dev.rosewood.rosechat.message.tokenizer.composer.decorator;

import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.decorator.bungee.BungeeTokenDecorator;
import net.md_5.bungee.api.chat.BaseComponent;

public class BungeeTokenDecorators extends TokenDecorators<BungeeTokenDecorator> {

    public BungeeTokenDecorators() {
        super(BungeeTokenDecorator.class);
    }

    public BungeeTokenDecorators(BungeeTokenDecorators decorators) {
        this();

        this.add(decorators.decorators);
    }

    public void apply(BaseComponent component, MessageTokenizer tokenizer, Token parent) {
        for (BungeeTokenDecorator decorator : this.decorators)
            decorator.apply(component, tokenizer, parent);
    }

}

package dev.rosewood.rosechat.message.tokenizer.composer.decorator.bungee;

import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.composer.decorator.TokenDecorators;
import net.md_5.bungee.api.chat.BaseComponent;

public class BungeeTokenDecorators extends TokenDecorators<BungeeTokenDecorator<?>> {

    @SuppressWarnings("unchecked")
    public BungeeTokenDecorators() {
        super(BungeeTokenDecorator::from, (Class<BungeeTokenDecorator<?>>) (Class<?>) BungeeTokenDecorator.class);
    }

    public BungeeTokenDecorators(BungeeTokenDecorators decorators) {
        this();

        this.add(decorators.decorators);
    }

    public void apply(BaseComponent component, Token parent) {
        for (BungeeTokenDecorator<?> decorator : this.decorators)
            decorator.apply(component, parent);
    }

}

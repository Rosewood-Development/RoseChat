package dev.rosewood.rosechat.message.tokenizer.composer.decorator.adventure;

import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.composer.decorator.TokenDecorators;
import net.kyori.adventure.text.Component;

public class AdventureTokenDecorators extends TokenDecorators<AdventureTokenDecorator<?>> {

    @SuppressWarnings("unchecked")
    public AdventureTokenDecorators() {
        super(AdventureTokenDecorator::from, (Class<AdventureTokenDecorator<?>>) (Class<?>) AdventureTokenDecorator.class);
    }

    public AdventureTokenDecorators(AdventureTokenDecorators decorators) {
        this();

        this.add(decorators.decorators);
    }

    public Component apply(Component component, Token parent) {
        for (AdventureTokenDecorator<?> decorator : this.decorators)
            component = decorator.apply(component, parent);
        return component;
    }

}

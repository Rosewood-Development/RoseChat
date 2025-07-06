package dev.rosewood.rosechat.message.tokenizer.composer.decorator;

import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.decorator.adventure.AdventureTokenDecorator;
import net.kyori.adventure.text.Component;

public class AdventureTokenDecorators extends TokenDecorators<AdventureTokenDecorator> {

    public AdventureTokenDecorators() {
        super(AdventureTokenDecorator.class);
    }

    public AdventureTokenDecorators(AdventureTokenDecorators decorators) {
        this();

        this.add(decorators.decorators);
    }

    public Component apply(Component component, MessageTokenizer tokenizer, Token parent) {
        for (AdventureTokenDecorator decorator : this.decorators)
            component = decorator.apply(component, tokenizer, parent);
        return component;
    }

}

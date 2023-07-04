package dev.rosewood.rosechat.message.tokenizer;

import dev.rosewood.rosechat.message.tokenizer.decorator.TokenDecorator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class TokenDecorators {

    private final List<TokenDecorator> decorators;

    public TokenDecorators() {
        this.decorators = new ArrayList<>();
    }

    public void add(List<TokenDecorator> toAdd) {
        Iterator<TokenDecorator> existingDecorators = this.decorators.iterator();
        while (existingDecorators.hasNext()) {
            TokenDecorator existingDecorator = existingDecorators.next();
            for (TokenDecorator newDecorator : toAdd) {
                if (existingDecorator.isOverwrittenBy(newDecorator)) {
                    existingDecorators.remove();
                    break;
                }
            }
        }

        for (TokenDecorator decorator : toAdd)
            if (!decorator.isMarker())
                this.decorators.add(decorator);
    }

    public void apply(ComponentBuilder builder) {
        for (TokenDecorator decorator : this.decorators)
            decorator.apply(builder.getCurrentComponent());
    }

    /**
     * Creates a copy of this TokenDecorators with additional decorators added.
     *
     * @param toAdd The decorators to add
     * @return a copy of this TokenDecorators with additional decorators added
     */
    public TokenDecorators copyWith(List<TokenDecorator> toAdd) {
        TokenDecorators newDecorators = new TokenDecorators();
        newDecorators.add(this.decorators);
        newDecorators.add(toAdd);
        return newDecorators;
    }

}

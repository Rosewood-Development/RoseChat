package dev.rosewood.rosechat.message.tokenizer.decorator;

import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TokenDecorators {

    protected final List<TokenDecorator> decorators;

    public TokenDecorators() {
        this.decorators = new ArrayList<>();
    }

    public TokenDecorators(TokenDecorators decorators) {
        this();
        
        this.add(decorators.decorators);
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

    public void apply(BaseComponent component, MessageTokenizer tokenizer, Token parent) {
        for (TokenDecorator decorator : this.decorators)
            decorator.apply(component, tokenizer, parent);
    }

    public boolean blocksTextStitching() {
        for (TokenDecorator decorator : this.decorators)
            if (decorator.blocksTextStitching())
                return true;

        return false;
    }

}

package dev.rosewood.rosechat.message.tokenizer.composer.decorator;

import dev.rosewood.rosechat.message.tokenizer.decorator.TokenDecorator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class TokenDecorators<T extends TokenDecorator> {

    private final Class<T> type;
    protected final List<T> decorators;

    public TokenDecorators(Class<T> type) {
        this.type = type;
        this.decorators = new ArrayList<>();
    }

    public void add(List<? extends TokenDecorator> toAdd) {
        Iterator<T> existingDecorators = this.decorators.iterator();
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
            if (!decorator.isMarker() && this.type.isInstance(decorator))
                this.decorators.add(this.type.cast(decorator));
    }

    public boolean blocksTextStitching() {
        for (TokenDecorator decorator : this.decorators)
            if (decorator.blocksTextStitching())
                return true;

        return false;
    }

}

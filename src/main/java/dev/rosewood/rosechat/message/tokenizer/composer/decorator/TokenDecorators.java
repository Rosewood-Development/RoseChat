package dev.rosewood.rosechat.message.tokenizer.composer.decorator;

import dev.rosewood.rosechat.message.tokenizer.decorator.TokenDecorator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public abstract class TokenDecorators<T extends TokenDecorator> {

    private final Function<TokenDecorator, T> wrapperFunction;
    private final Class<T> wrappedType;
    protected final List<T> decorators;

    public TokenDecorators(Function<TokenDecorator, T> wrapperFunction, Class<T> wrappedType) {
        this.wrapperFunction = wrapperFunction;
        this.wrappedType = wrappedType;
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

        for (TokenDecorator decorator : toAdd) {
            if (!decorator.getRoot().isMarker()) {
                if (this.wrappedType.isInstance(decorator)) {
                    this.decorators.add(this.wrappedType.cast(decorator));
                } else {
                    this.decorators.add(this.wrapperFunction.apply(decorator));
                }
            }
        }
    }

    public boolean blocksTextStitching() {
        for (TokenDecorator decorator : this.decorators)
            if (decorator.getRoot().blocksTextStitching())
                return true;

        return false;
    }

}

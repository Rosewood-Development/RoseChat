package dev.rosewood.rosechat.message.tokenizer.decorator;

import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import java.util.function.BiFunction;
import net.md_5.bungee.api.chat.BaseComponent;

public abstract class TokenDecorator {

    private final DecoratorType type;
    protected final String content;

    protected TokenDecorator(String content, DecoratorType type) {
        this.content = content;
        this.type = type;
    }

    protected TokenDecorator(DecoratorType type) {
        this(null, type);
    }

    /**
     * Applies this decorator to the given component.
     *
     * @param component The component to apply this decorator to
     * @param tokenizer The tokenizer
     */
    public abstract void apply(BaseComponent component, MessageTokenizer tokenizer);

    /**
     * Checks if this decorator is overwritten by the given decorator.
     *
     * @param newDecorator The decorator to check compatibility with
     * @return true if this decorator is overwritten by the given decorator
     */
    protected abstract boolean isOverwrittenBy(TokenDecorator newDecorator);

    /**
     * Checks if this decorator is a marker decorator.
     * Marker decorators do not get added to the list of decorators and are only used to overwrite other decorators.
     *
     * @return true if this decorator is a marker decorator
     */
    protected boolean isMarker() {
        return false;
    }

    /**
     * @return the type of this decorator
     */
    public DecoratorType getType() {
        return this.type;
    }

}

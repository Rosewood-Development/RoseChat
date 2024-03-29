package dev.rosewood.rosechat.message.tokenizer.decorator;

import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import net.md_5.bungee.api.chat.BaseComponent;

public abstract class TokenDecorator {

    private final DecoratorType type;

    protected TokenDecorator(DecoratorType type) {
        this.type = type;
    }

    /**
     * Applies this decorator to the given component.
     *
     * @param component The component to apply this decorator to
     * @param tokenizer The tokenizer
     * @param parent The parent token
     */
    public abstract void apply(BaseComponent component, MessageTokenizer tokenizer, Token parent);

    /**
     * Checks if this decorator is overwritten by the given decorator.
     * Default implementation checks if the given decorator is the same class.
     *
     * @param newDecorator The decorator to check compatibility with
     * @return true if this decorator is overwritten by the given decorator
     */
    public boolean isOverwrittenBy(TokenDecorator newDecorator) {
        return this.getClass() == newDecorator.getClass();
    }

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
     * Checks if this decorator blocks text stitching.
     * Text stitching is the process of combining multiple text components into one.
     *
     * @return true if this decorator blocks text stitching
     */
    public boolean blocksTextStitching() {
        return false;
    }

    /**
     * @return the type of this decorator
     */
    public DecoratorType getType() {
        return this.type;
    }

}

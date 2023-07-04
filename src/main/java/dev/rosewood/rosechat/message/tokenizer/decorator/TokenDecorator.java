package dev.rosewood.rosechat.message.tokenizer.decorator;

import net.md_5.bungee.api.chat.BaseComponent;

public interface TokenDecorator {

    void apply(BaseComponent component);

    /**
     * Checks if this decorator is overwritten by the given decorator.
     *
     * @param newDecorator The decorator to check compatibility with
     * @return true if this decorator is overwritten by the given decorator
     */
    boolean isOverwrittenBy(TokenDecorator newDecorator);

    /**
     * Checks if this decorator is a marker decorator.
     * Marker decorators do not get added to the list of decorators and are only used to overwrite other decorators.
     *
     * @return true if this decorator is a marker decorator
     */
    default boolean isMarker() {
        return false;
    }

}

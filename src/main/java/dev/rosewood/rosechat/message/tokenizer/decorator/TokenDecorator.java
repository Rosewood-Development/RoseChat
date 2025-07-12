package dev.rosewood.rosechat.message.tokenizer.decorator;

import dev.rosewood.rosechat.message.tokenizer.Token;

public interface TokenDecorator {

    /**
     * @return the type of this decorator
     */
    DecoratorType getType();

    /**
     * Checks if this decorator is overwritten by the given decorator.
     * Default implementation checks if the given decorator is the same class.
     *
     * @param newDecorator The decorator to check compatibility with
     * @return true if this decorator is overwritten by the given decorator
     */
    default boolean isOverwrittenBy(TokenDecorator newDecorator) {
        return this.getRoot().getClass() == newDecorator.getRoot().getClass();
    }

    default TokenDecorator getRoot() {
        return this;
    }

    /**
     * Checks if this decorator is a marker decorator.
     * Marker decorators do not get added to the list of decorators and are only used to overwrite other decorators.
     *
     * @return true if this decorator is a marker decorator
     */
    default boolean isMarker() {
        return false;
    }

    /**
     * Checks if this decorator blocks text stitching.
     * Text stitching is the process of combining multiple text components into one.
     *
     * @return true if this decorator blocks text stitching
     */
    default boolean blocksTextStitching() {
        return false;
    }

    /**
     * @return the content of this Token as a builder if {@link #getType()} is {@code CONTENT}, nullable, will be tokenized
     */
    default Token.Builder getContent() {
        return null;
    }

    /**
     * Set the tokenized content of this token if {@link #getType()} is {@code CONTENT}, does nothing otherwise
     * @param content the tokenized content to set
     */
    default void setContentToken(Token content) {

    }

    /**
     * @return the content token, if set
     */
    default Token getContentToken() {
        return null;
    }

}

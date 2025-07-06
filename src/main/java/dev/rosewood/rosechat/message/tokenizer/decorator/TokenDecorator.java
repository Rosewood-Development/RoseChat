package dev.rosewood.rosechat.message.tokenizer.decorator;

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
        return this.getClass() == newDecorator.getClass();
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

}

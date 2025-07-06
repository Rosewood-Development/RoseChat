package dev.rosewood.rosechat.message.tokenizer.decorator;

public abstract class FontDecorator implements TokenDecorator {

    protected final String font;

    protected FontDecorator(String font) {
        this.font = font;
    }

    @Override
    public DecoratorType getType() {
        return DecoratorType.STYLING;
    }

}

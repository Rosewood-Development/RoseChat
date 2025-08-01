package dev.rosewood.rosechat.message.tokenizer.decorator;

public record FontDecorator(String font) implements TokenDecorator {

    @Override
    public DecoratorType getType() {
        return DecoratorType.STYLING;
    }

    @Override
    public boolean isOverwrittenBy(TokenDecorator newDecorator) {
        return newDecorator instanceof FontDecorator;
    }

}

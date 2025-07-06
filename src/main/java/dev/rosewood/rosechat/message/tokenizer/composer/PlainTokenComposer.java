package dev.rosewood.rosechat.message.tokenizer.composer;

import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.TokenType;
import dev.rosewood.rosechat.message.tokenizer.decorator.DecoratorFactory;

public class PlainTokenComposer implements TokenComposer<String> {

    protected PlainTokenComposer() {

    }

    @Override
    public String compose(Token token) {
        StringBuilder builder = new StringBuilder();
        this.compose(token, builder);
        return builder.toString();
    }

    private void compose(Token token, StringBuilder stringBuilder) {
        if (token.getType() != TokenType.GROUP)
            throw new IllegalStateException("Cannot convert a token that is not of type GROUP");

        for (Token child : token.getChildren()) {
            switch (child.getType()) {
                case TEXT -> stringBuilder.append(child.getContent());
                case GROUP -> this.compose(child, stringBuilder);
            }
        }
    }

    @Override
    public DecoratorFactory decorators() {
        return DecoratorFactory.any();
    }

}

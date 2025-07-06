package dev.rosewood.rosechat.message.tokenizer.composer.adventure;

import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.TokenType;
import dev.rosewood.rosechat.message.tokenizer.composer.TokenComposer;
import dev.rosewood.rosechat.message.tokenizer.composer.decorator.AdventureTokenDecorators;
import dev.rosewood.rosechat.message.tokenizer.decorator.DecoratorFactory;
import net.kyori.adventure.text.Component;

public class FullyDecoratedAdventureTokenComposer implements TokenComposer<Component> {

    private final MessageTokenizer tokenizer;

    protected FullyDecoratedAdventureTokenComposer(MessageTokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    @Override
    public Component compose(Token token) {
        return this.compose(token, this.createDecorators());
    }

    protected Component compose(Token token, AdventureTokenDecorators contextDecorators) {
        if (token.getType() != TokenType.GROUP)
            throw new IllegalStateException("Cannot convert a token that is not of type GROUP");

        Component componentBuilder = Component.empty();
        StringBuilder contentBuilder = new StringBuilder();

        for (Token child : token.getChildren()) {
            if ((child.getType() != TokenType.TEXT || contextDecorators.blocksTextStitching()) && !contentBuilder.isEmpty())
                componentBuilder = this.applyAndDecorate(componentBuilder, contentBuilder, child, contextDecorators);

            switch (child.getType()) {
                case TEXT -> contentBuilder.append(child.getContent());
                case DECORATOR -> contextDecorators.add(child.getDecorators());
                case GROUP -> {
                    AdventureTokenDecorators childDecorators = child.shouldEncapsulate() ? this.createDecorators(contextDecorators) : contextDecorators;
                    componentBuilder = componentBuilder.append(this.compose(child, childDecorators));
                }
            }
        }

        if (!contentBuilder.isEmpty())
            componentBuilder = this.applyAndDecorate(componentBuilder, contentBuilder, token, contextDecorators);

        if (token.isPlain())
            return componentBuilder;

        Component wrapperComponent = Component.textOfChildren(componentBuilder);
        AdventureTokenDecorators wrapperDecorators = this.createDecorators();
        wrapperDecorators.add(token.getDecorators());
        wrapperComponent = wrapperDecorators.apply(wrapperComponent, this.tokenizer, token);
        return wrapperComponent;
    }

    protected AdventureTokenDecorators createDecorators() {
        return new AdventureTokenDecorators();
    }

    protected AdventureTokenDecorators createDecorators(AdventureTokenDecorators contextDecorators) {
        return new AdventureTokenDecorators(contextDecorators);
    }

    private Component applyAndDecorate(Component component, StringBuilder contentBuilder, Token token, AdventureTokenDecorators contextDecorators) {
        String content = contentBuilder.toString();
        contentBuilder.setLength(0);

        if (contextDecorators.blocksTextStitching()) {
            for (char c : content.toCharArray())
                component = component.append(contextDecorators.apply(Component.text(c), this.tokenizer, token));
            return component;
        } else {
            return component.append(contextDecorators.apply(Component.text(content), this.tokenizer, token));
        }
    }

    @Override
    public DecoratorFactory decorators() {
        return DecoratorFactory.adventure();
    }

}

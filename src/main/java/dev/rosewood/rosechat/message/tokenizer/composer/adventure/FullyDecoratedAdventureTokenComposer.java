package dev.rosewood.rosechat.message.tokenizer.composer.adventure;

import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.TokenType;
import dev.rosewood.rosechat.message.tokenizer.composer.TokenComposer;
import dev.rosewood.rosechat.message.tokenizer.composer.decorator.adventure.AdventureTokenDecorators;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class FullyDecoratedAdventureTokenComposer implements TokenComposer<Component> {

    public static final FullyDecoratedAdventureTokenComposer INSTANCE = new FullyDecoratedAdventureTokenComposer();

    protected FullyDecoratedAdventureTokenComposer() {

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
        wrapperComponent = wrapperDecorators.apply(wrapperComponent, token);
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
                component = component.append(contextDecorators.apply(Component.text(c), token));
            return component;
        } else {
            return component.append(contextDecorators.apply(Component.text(content), token));
        }
    }

    @Override
    public Component composeLegacyText(String text) {
        return LegacyComponentSerializer.legacySection().deserialize(text);
    }

    @Override
    public Component composeJson(String json) {
        return GsonComponentSerializer.gson().deserialize(json);
    }

}

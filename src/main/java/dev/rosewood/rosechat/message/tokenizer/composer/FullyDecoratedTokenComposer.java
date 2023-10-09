package dev.rosewood.rosechat.message.tokenizer.composer;

import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.TokenType;
import dev.rosewood.rosechat.message.tokenizer.decorator.TokenDecorators;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;

public class FullyDecoratedTokenComposer implements TokenComposer<BaseComponent[]> {

    private final MessageTokenizer tokenizer;

    protected FullyDecoratedTokenComposer(MessageTokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    @Override
    public BaseComponent[] compose(Token token) {
        return this.compose(token, new TokenDecorators());
    }

    protected BaseComponent[] compose(Token token, TokenDecorators contextDecorators) {
        if (token.getType() != TokenType.GROUP)
            throw new IllegalStateException("Cannot convert a token that is not of type GROUP");

        ComponentBuilder componentBuilder = new ComponentBuilder();
        StringBuilder contentBuilder = new StringBuilder();

        for (Token child : token.getChildren()) {
            if ((child.getType() != TokenType.TEXT || contextDecorators.blocksTextStitching()) && !contentBuilder.isEmpty())
                this.applyAndDecorate(componentBuilder, contentBuilder, child, contextDecorators);

            switch (child.getType()) {
                case TEXT -> contentBuilder.append(child.getContent());
                case DECORATOR -> contextDecorators.add(child.getDecorators());
                case GROUP -> {
                    TokenDecorators childDecorators = child.shouldEncapsulate() ? new TokenDecorators(contextDecorators) : contextDecorators;
                    for (BaseComponent component : this.compose(child, childDecorators))
                        componentBuilder.append(component, ComponentBuilder.FormatRetention.NONE);
                }
            }
        }

        if (!contentBuilder.isEmpty())
            this.applyAndDecorate(componentBuilder, contentBuilder, token, contextDecorators);

        BaseComponent[] components = componentBuilder.create();
        if (token.isPlain() || components.length == 0)
            return components;

        TextComponent wrapperComponent = new TextComponent(components);
        token.getDecorators().forEach(x -> x.apply(wrapperComponent, this.tokenizer, token));
        return new BaseComponent[]{wrapperComponent};
    }

    private void applyAndDecorate(ComponentBuilder componentBuilder, StringBuilder contentBuilder, Token token, TokenDecorators contextDecorators) {
        String content = contentBuilder.toString();
        contentBuilder.setLength(0);

        if (contextDecorators.blocksTextStitching()) {
            for (char c : content.toCharArray()) {
                componentBuilder.append(String.valueOf(c), ComponentBuilder.FormatRetention.NONE);
                contextDecorators.apply(componentBuilder, this.tokenizer, token);
            }
        } else {
            componentBuilder.append(content, ComponentBuilder.FormatRetention.NONE);
            contextDecorators.apply(componentBuilder, this.tokenizer, token);
        }
    }

}

package dev.rosewood.rosechat.message.tokenizer.composer;

import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.TokenType;
import dev.rosewood.rosechat.message.tokenizer.composer.decorator.bungee.BungeeTokenDecorators;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class FullyDecoratedBungeeChatComposer implements ChatComposer<BaseComponent[]> {

    public static final FullyDecoratedBungeeChatComposer INSTANCE = new FullyDecoratedBungeeChatComposer();

    protected FullyDecoratedBungeeChatComposer() {

    }

    @Override
    public BaseComponent[] compose(Token token) {
        return this.compose(token, this.createDecorators());
    }

    protected BaseComponent[] compose(Token token, BungeeTokenDecorators contextDecorators) {
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
                    BungeeTokenDecorators childDecorators = child.shouldEncapsulate() ? this.createDecorators(contextDecorators) : contextDecorators;
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
        BungeeTokenDecorators wrapperDecorators = this.createDecorators();
        wrapperDecorators.add(token.getDecorators());
        wrapperDecorators.apply(wrapperComponent, token);
        return new BaseComponent[]{wrapperComponent};
    }

    protected BungeeTokenDecorators createDecorators() {
        return new BungeeTokenDecorators();
    }

    protected BungeeTokenDecorators createDecorators(BungeeTokenDecorators contextDecorators) {
        return new BungeeTokenDecorators(contextDecorators);
    }

    private void applyAndDecorate(ComponentBuilder componentBuilder, StringBuilder contentBuilder, Token token, BungeeTokenDecorators contextDecorators) {
        String content = contentBuilder.toString();
        contentBuilder.setLength(0);

        if (contextDecorators.blocksTextStitching()) {
            for (char c : content.toCharArray()) {
                componentBuilder.append(String.valueOf(c), ComponentBuilder.FormatRetention.NONE);
                contextDecorators.apply(componentBuilder.getCurrentComponent(), token);
            }
        } else {
            componentBuilder.append(content, ComponentBuilder.FormatRetention.NONE);
            contextDecorators.apply(componentBuilder.getCurrentComponent(), token);
        }
    }

    @Override
    public BaseComponent[] composeLegacy(String text) {
        return TextComponent.fromLegacyText(text);
    }

    @Override
    public BaseComponent[] composeJson(String json) {
        return ComponentSerializer.parse(json);
    }

    @Override
    public BaseComponent[] composeBungee(BaseComponent[] components) {
        return components;
    }

    @Override
    public ChatComposer.Adventure<BaseComponent[]> composeAdventure() {
        return Adventure.INSTANCE;
    }

    public static final class Adventure implements ChatComposer.Adventure<BaseComponent[]> {

        private static final Adventure INSTANCE = new Adventure();

        private Adventure() {

        }

        @Override
        public BaseComponent[] compose(Component component) {
            return BungeeComponentSerializer.get().serialize(component);
        }

    }

}

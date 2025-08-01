package dev.rosewood.rosechat.message.tokenizer.composer.decorator.bungee;

import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.decorator.ClickDecorator;
import dev.rosewood.rosechat.message.tokenizer.decorator.ColorDecorator;
import dev.rosewood.rosechat.message.tokenizer.decorator.DecoratorType;
import dev.rosewood.rosechat.message.tokenizer.decorator.FontDecorator;
import dev.rosewood.rosechat.message.tokenizer.decorator.FormatDecorator;
import dev.rosewood.rosechat.message.tokenizer.decorator.HoverDecorator;
import dev.rosewood.rosechat.message.tokenizer.decorator.ShadowColorDecorator;
import dev.rosewood.rosechat.message.tokenizer.decorator.TokenDecorator;
import net.md_5.bungee.api.chat.BaseComponent;

public abstract class BungeeTokenDecorator<T extends TokenDecorator> implements TokenDecorator {

    protected final T decorator;

    public BungeeTokenDecorator(T decorator) {
        this.decorator = decorator;
    }

    @Override
    public DecoratorType getType() {
        return this.decorator.getType();
    }

    @Override
    public boolean isOverwrittenBy(TokenDecorator newDecorator) {
        if (newDecorator instanceof BungeeTokenDecorator<?> bungeeTokenDecorator) {
            return this.decorator.isOverwrittenBy(bungeeTokenDecorator.decorator);
        } else {
            return this.decorator.isOverwrittenBy(newDecorator);
        }
    }

    @Override
    public boolean isMarker() {
        return this.decorator.isMarker();
    }

    @Override
    public boolean blocksTextStitching() {
        return this.decorator.blocksTextStitching();
    }

    @Override
    public Token.Builder getContent() {
        return this.decorator.getContent();
    }

    @Override
    public void setContentToken(Token content) {
        throw new IllegalStateException("Immutable");
    }

    @Override
    public Token getContentToken() {
        return this.decorator.getContentToken();
    }

    /**
     * Applies this decorator to the given component.
     *
     * @param component The component to apply this decorator to
     * @param parent The parent token
     */
    public abstract void apply(BaseComponent component, Token parent);

    @SuppressWarnings("unchecked")
    static <T extends TokenDecorator> BungeeTokenDecorator<T> from(T decorator) {
        return (BungeeTokenDecorator<T>) switch (decorator) {
            case ClickDecorator clickDecorator -> new BungeeClickDecorator(clickDecorator);
            case ColorDecorator colorDecorator -> new BungeeColorDecorator(colorDecorator);
            case ShadowColorDecorator shadowColorDecorator -> new BungeeShadowColorDecorator(shadowColorDecorator);
            case FontDecorator fontDecorator -> new BungeeFontDecorator(fontDecorator);
            case FormatDecorator formatDecorator -> new BungeeFormatDecorator(formatDecorator);
            case HoverDecorator hoverDecorator -> new BungeeHoverDecorator(hoverDecorator);
            default -> throw new IllegalArgumentException("Unhandled decorator type: " + decorator.getClass().getName());
        };
    }

}

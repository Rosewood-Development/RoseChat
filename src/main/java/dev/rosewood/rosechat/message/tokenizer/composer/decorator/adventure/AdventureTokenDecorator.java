package dev.rosewood.rosechat.message.tokenizer.composer.decorator.adventure;

import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.composer.decorator.bungee.BungeeTokenDecorator;
import dev.rosewood.rosechat.message.tokenizer.decorator.ClickDecorator;
import dev.rosewood.rosechat.message.tokenizer.decorator.ColorDecorator;
import dev.rosewood.rosechat.message.tokenizer.decorator.DecoratorType;
import dev.rosewood.rosechat.message.tokenizer.decorator.FontDecorator;
import dev.rosewood.rosechat.message.tokenizer.decorator.FormatDecorator;
import dev.rosewood.rosechat.message.tokenizer.decorator.HoverDecorator;
import dev.rosewood.rosechat.message.tokenizer.decorator.ShadowColorDecorator;
import dev.rosewood.rosechat.message.tokenizer.decorator.TokenDecorator;
import net.kyori.adventure.text.Component;

public abstract class AdventureTokenDecorator<T extends TokenDecorator> implements TokenDecorator {

    protected final T decorator;

    public AdventureTokenDecorator(T decorator) {
        this.decorator = decorator;
    }

    @Override
    public DecoratorType getType() {
        return this.decorator.getType();
    }

    @Override
    public boolean isOverwrittenBy(TokenDecorator newDecorator) {
        if (newDecorator instanceof AdventureTokenDecorator<?> bungeeTokenDecorator) {
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
     * Applies and returns a new component with the decorator applied.
     *
     * @param component The component to apply this decorator to
     * @param parent The parent token
     * @return A new component with the decorator applied
     */
    public abstract Component apply(Component component, Token parent);

    @SuppressWarnings("unchecked")
    static <T extends TokenDecorator> AdventureTokenDecorator<T> from(T decorator) {
        return (AdventureTokenDecorator<T>) switch (decorator) {
            case ClickDecorator clickDecorator -> new AdventureClickDecorator(clickDecorator);
            case ColorDecorator colorDecorator -> new AdventureColorDecorator(colorDecorator);
            case ShadowColorDecorator shadowColorDecorator -> new AdventureShadowColorDecorator(shadowColorDecorator);
            case FontDecorator fontDecorator -> new AdventureFontDecorator(fontDecorator);
            case FormatDecorator formatDecorator -> new AdventureFormatDecorator(formatDecorator);
            case HoverDecorator hoverDecorator -> new AdventureHoverDecorator(hoverDecorator);
            default -> throw new IllegalArgumentException("Unhandled decorator type: " + decorator.getClass().getName());
        };
    }

}

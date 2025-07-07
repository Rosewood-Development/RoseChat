package dev.rosewood.rosechat.message.tokenizer.composer.decorator.bungee;

import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.decorator.ClickDecorator;
import dev.rosewood.rosechat.message.tokenizer.decorator.ColorDecorator;
import dev.rosewood.rosechat.message.tokenizer.decorator.DecoratorType;
import dev.rosewood.rosechat.message.tokenizer.decorator.FontDecorator;
import dev.rosewood.rosechat.message.tokenizer.decorator.FormatDecorator;
import dev.rosewood.rosechat.message.tokenizer.decorator.HoverDecorator;
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
            case FontDecorator fontDecorator -> new BungeeFontDecorator(fontDecorator);
            case FormatDecorator formatDecorator -> new BungeeFormatDecorator(formatDecorator);
            case HoverDecorator hoverDecorator -> new BungeeHoverDecorator(hoverDecorator);
            default -> throw new IllegalArgumentException("Unhandled decorator type: " + decorator.getClass().getName());
        };
    }

}

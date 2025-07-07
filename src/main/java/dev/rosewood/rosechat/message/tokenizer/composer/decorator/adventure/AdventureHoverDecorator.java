package dev.rosewood.rosechat.message.tokenizer.composer.decorator.adventure;

import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.composer.TokenComposer;
import dev.rosewood.rosechat.message.tokenizer.decorator.HoverDecorator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;

public class AdventureHoverDecorator extends AdventureTokenDecorator<HoverDecorator> {

    public AdventureHoverDecorator(HoverDecorator decorator) {
        super(decorator);
    }

    @Override
    public Component apply(Component component, Token parent) {
        return switch (this.decorator.action()) {
            case SHOW_TEXT -> component.hoverEvent(HoverEvent.showText(TokenComposer.adventure().styles().compose(this.decorator.getContentToken())));
            case SHOW_ITEM -> component.hoverEvent(this.decorator.item().asHoverEvent());
        };
    }

}

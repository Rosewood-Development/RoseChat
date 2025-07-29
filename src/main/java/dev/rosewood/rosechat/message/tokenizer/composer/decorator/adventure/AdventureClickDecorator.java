package dev.rosewood.rosechat.message.tokenizer.composer.decorator.adventure;

import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.decorator.ClickDecorator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;

public class AdventureClickDecorator extends AdventureTokenDecorator<ClickDecorator> {

    private final ClickEvent.Action clickEventAction;

    public AdventureClickDecorator(ClickDecorator decorator) {
        super(decorator);
        this.clickEventAction = switch (decorator.action()) {
            case OPEN_URL -> ClickEvent.Action.OPEN_URL;
            case OPEN_FILE -> ClickEvent.Action.OPEN_FILE;
            case RUN_COMMAND -> ClickEvent.Action.RUN_COMMAND;
            case SUGGEST_COMMAND -> ClickEvent.Action.SUGGEST_COMMAND;
            case CHANGE_PAGE -> ClickEvent.Action.CHANGE_PAGE;
            case COPY_TO_CLIPBOARD -> ClickEvent.Action.COPY_TO_CLIPBOARD;
        };
    }

    @Override
    public Component apply(Component component, Token parent) {
        String value = parent.getPlaceholders().apply(this.decorator.value());
        if (this.decorator.action() == ClickDecorator.Action.OPEN_URL && !ClickDecorator.PATTERN.matcher(value).find())
            value = "https://" + value;

        return component.clickEvent(net.kyori.adventure.text.event.ClickEvent.clickEvent(this.clickEventAction, value));
    }

}

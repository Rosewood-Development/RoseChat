package dev.rosewood.rosechat.message.tokenizer.composer.decorator.bungee;

import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.decorator.ClickDecorator;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;

public class BungeeClickDecorator extends BungeeTokenDecorator<ClickDecorator> {

    private final ClickEvent.Action clickEventAction;

    public BungeeClickDecorator(ClickDecorator clickDecorator) {
        super(clickDecorator);
        this.clickEventAction = switch (clickDecorator.action()) {
            case OPEN_URL -> ClickEvent.Action.OPEN_URL;
            case OPEN_FILE -> ClickEvent.Action.OPEN_FILE;
            case RUN_COMMAND -> ClickEvent.Action.RUN_COMMAND;
            case SUGGEST_COMMAND -> ClickEvent.Action.SUGGEST_COMMAND;
            case CHANGE_PAGE -> ClickEvent.Action.CHANGE_PAGE;
            case COPY_TO_CLIPBOARD -> ClickEvent.Action.COPY_TO_CLIPBOARD;
        };
    }

    @Override
    public void apply(BaseComponent component, Token parent) {
        String value = parent.getPlaceholders().apply(this.decorator.value());
        if (this.decorator.action() == ClickDecorator.Action.OPEN_URL && !ClickDecorator.PATTERN.matcher(value).find())
            value = "https://" + value;

        component.setClickEvent(new ClickEvent(this.clickEventAction, parent.getPlaceholders().apply(value)));
    }

}

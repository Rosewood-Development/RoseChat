package dev.rosewood.rosechat.message.tokenizer.decorator.adventure;

import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.decorator.ClickDecorator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;

public class AdventureClickDecorator extends ClickDecorator implements AdventureTokenDecorator {

    private final ClickEvent.Action clickEventAction;

    public AdventureClickDecorator(Action action, String value) {
        super(action, value);
        this.clickEventAction = switch (this.action) {
            case OPEN_URL -> ClickEvent.Action.OPEN_URL;
            case OPEN_FILE -> ClickEvent.Action.OPEN_FILE;
            case RUN_COMMAND -> ClickEvent.Action.RUN_COMMAND;
            case SUGGEST_COMMAND -> ClickEvent.Action.SUGGEST_COMMAND;
            case CHANGE_PAGE -> ClickEvent.Action.CHANGE_PAGE;
            case COPY_TO_CLIPBOARD -> ClickEvent.Action.COPY_TO_CLIPBOARD;
        };
    }

    @Override
    public Component apply(Component component, MessageTokenizer tokenizer, Token parent) {
        String value = parent.getPlaceholders().apply(this.value);
        if (this.action == Action.OPEN_URL && !PATTERN.matcher(value).find())
            value = "https://" + value;

        return component.clickEvent(net.kyori.adventure.text.event.ClickEvent.clickEvent(this.clickEventAction, value));
    }

}

package dev.rosewood.rosechat.message.tokenizer.decorator.bungee;

import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.decorator.ClickDecorator;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;

public class BungeeClickDecorator extends ClickDecorator implements BungeeTokenDecorator {

    private final ClickEvent.Action clickEventAction;

    public BungeeClickDecorator(Action action, String value) {
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
    public void apply(BaseComponent component, MessageTokenizer tokenizer, Token parent) {
        String value = parent.getPlaceholders().apply(this.value);
        if (this.action == Action.OPEN_URL && !PATTERN.matcher(value).find())
            value = "https://" + value;

        component.setClickEvent(new ClickEvent(this.clickEventAction, parent.getPlaceholders().apply(value)));
    }

}

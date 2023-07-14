package dev.rosewood.rosechat.message.tokenizer.decorator;

import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import java.util.Objects;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;

public class ClickDecorator extends TokenDecorator {

    private final String value;
    private final ClickEvent.Action action;

    protected ClickDecorator(ClickEvent.Action action, String value) {
        super(DecoratorType.ACTION);
        this.value = value;
        this.action = action;
    }

    @Override
    public void apply(BaseComponent component, MessageTokenizer tokenizer, Token parent) {
        component.setClickEvent(new ClickEvent(this.action, parent.getPlaceholders().apply(this.value)));
    }

    public static ClickDecorator of(ClickEvent.Action action, String value) {
        return new ClickDecorator(Objects.requireNonNullElse(action, ClickEvent.Action.SUGGEST_COMMAND), value);
    }

}

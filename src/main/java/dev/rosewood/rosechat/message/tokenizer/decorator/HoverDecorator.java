package dev.rosewood.rosechat.message.tokenizer.decorator;

import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import java.util.Objects;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;

@SuppressWarnings("deprecation")
public class HoverDecorator extends TokenDecorator {

    private final HoverEvent.Action action;

    protected HoverDecorator(HoverEvent.Action action, String content) {
        super(content, DecoratorType.CONTENT);
        this.action = action;
    }

    @Override
    public void apply(BaseComponent component, MessageTokenizer tokenizer, StringPlaceholders placeholders) {
        Token token = Token.group(this.content).placeholders(placeholders).build();
        tokenizer.tokenizeContent(token, 0);
        BaseComponent[] hover = tokenizer.toComponents(token, new TokenDecorators());
        if (hover.length > 0)
            component.setHoverEvent(new HoverEvent(this.action, hover));
    }

    public static HoverDecorator of(HoverEvent.Action action, String content) {
        return new HoverDecorator(Objects.requireNonNullElse(action, HoverEvent.Action.SHOW_TEXT), content);
    }

}

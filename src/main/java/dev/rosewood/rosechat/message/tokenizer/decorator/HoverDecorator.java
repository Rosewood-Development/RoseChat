package dev.rosewood.rosechat.message.tokenizer.decorator;

import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;

public class HoverDecorator extends TokenDecorator {

    private final HoverEvent.Action action;

    protected HoverDecorator(HoverEvent.Action action, String content) {
        super(content, DecoratorType.CONTENT);
        this.action = action;
    }

    @Override
    public void apply(BaseComponent component, MessageTokenizer tokenizer) {
        Token token = Token.group(this.content).build();
        tokenizer.tokenizeContent(token, 0);
        BaseComponent[] hover = tokenizer.toComponents(token, new TokenDecorators());
        if (hover.length > 0)
            component.setHoverEvent(new HoverEvent(this.action, hover));
    }

    @Override
    protected boolean isOverwrittenBy(TokenDecorator newDecorator) {
        return newDecorator instanceof HoverDecorator;
    }

    public static HoverDecorator of(HoverEvent.Action action, String content) {
        return new HoverDecorator(action, content);
    }

}

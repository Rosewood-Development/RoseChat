package dev.rosewood.rosechat.message.tokenizer.decorator;

import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.composer.TokenComposer;
import java.util.Objects;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;

@SuppressWarnings("deprecation")
public class HoverDecorator extends TokenDecorator {

    private final HoverEvent.Action action;
    private final String content;

    protected HoverDecorator(HoverEvent.Action action, String content) {
        super(DecoratorType.CONTENT);
        this.action = action;
        this.content = content;
    }

    @Override
    public void apply(BaseComponent component, MessageTokenizer tokenizer, Token parent) {
        if (this.content == null) return;

        Token.Builder builder = Token.group(this.content).placeholders(parent.getPlaceholders());
        parent.getIgnoredTokenizers().forEach(builder::ignoreTokenizer);

        Token token = builder.build();
        tokenizer.tokenizeContent(token, 0);
        BaseComponent[] hover = tokenizer.toComponents(token, TokenComposer.styles(tokenizer));
        if (hover.length > 0)
            component.setHoverEvent(new HoverEvent(this.action, hover));
    }

    public static HoverDecorator of(HoverEvent.Action action, String content) {
        return new HoverDecorator(Objects.requireNonNullElse(action, HoverEvent.Action.SHOW_TEXT), content);
    }

}

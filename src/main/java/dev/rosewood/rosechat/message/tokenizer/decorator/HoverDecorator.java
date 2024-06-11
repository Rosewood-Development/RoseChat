package dev.rosewood.rosechat.message.tokenizer.decorator;

import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.composer.TokenComposer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("deprecation")
public class HoverDecorator extends TokenDecorator {

    private final HoverEvent.Action action;
    private final List<String> content;

    protected HoverDecorator(HoverEvent.Action action, List<String> content) {
        super(DecoratorType.CONTENT);

        this.action = action;
        this.content = content;
    }

    @Override
    public void apply(BaseComponent component, MessageTokenizer tokenizer, Token parent) {
        if (this.content == null || this.content.isEmpty())
            return;

        if (this.action != HoverEvent.Action.SHOW_TEXT) {
            component.setHoverEvent(new HoverEvent(this.action, new ComponentBuilder(parent.getPlaceholders().apply(this.content.get(0))).create()));
            return;
        }

        ComponentBuilder componentBuilder = new ComponentBuilder();
        int index = 0;
        for (String s : this.content) {
            Token.Builder builder = Token.group(s).placeholders(parent.getPlaceholders());
            parent.getIgnoredTokenizers().forEach(builder::ignoreTokenizer);

            Token token = builder.build();
            tokenizer.tokenizeContent(token, 0);
            BaseComponent[] hover = tokenizer.toComponents(token, TokenComposer.styles(tokenizer));
            if (hover.length > 0) {
                componentBuilder.append(hover, ComponentBuilder.FormatRetention.ALL);
                if (index != this.content.size() - 1)
                    componentBuilder.append("\n").bold(false).italic(false).underlined(false).obfuscated(false).strikethrough(false);
            }

            index++;
        }

        component.setHoverEvent(new HoverEvent(this.action, componentBuilder.create()));
    }

    public static HoverDecorator of(HoverEvent.Action action, List<String> content) {
        return new HoverDecorator(Objects.requireNonNullElse(action, HoverEvent.Action.SHOW_TEXT), content);
    }

    public static HoverDecorator of(HoverEvent.Action action, String content) {
        return new HoverDecorator(Objects.requireNonNullElse(action, HoverEvent.Action.SHOW_TEXT), Collections.singletonList(content));
    }

}

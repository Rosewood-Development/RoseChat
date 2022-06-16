package dev.rosewood.rosechat.message.wrapper.tokenizer.tag;

import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;

public class TagToken extends Token {

    private final String content;
    private final String hover;
    private final String click;
    private final ClickEvent.Action clickAction;

    public TagToken(String originalContent, String content, String hover, String click, ClickEvent.Action clickAction) {
        super(originalContent);

        this.content = content;
        this.hover = hover;
        this.click = click;
        this.clickAction = clickAction;
    }

    @Override
    public String getContent() {
        return this.content;
    }

    @Override
    public boolean requiresTokenizing() {
        return true;
    }

    @Override
    public String getHover() {
        return this.hover;
    }

    @Override
    public HoverEvent.Action getHoverAction() {
        return HoverEvent.Action.SHOW_TEXT;
    }

    @Override
    public String getClick() {
        return this.click;
    }

    @Override
    public ClickEvent.Action getClickAction() {
        return this.clickAction;
    }
}

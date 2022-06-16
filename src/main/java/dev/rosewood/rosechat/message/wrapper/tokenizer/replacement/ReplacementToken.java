package dev.rosewood.rosechat.message.wrapper.tokenizer.replacement;

import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;

public class ReplacementToken extends Token {

    private final String content;
    private final String hover;
    private final String click;
    private final ClickEvent.Action clickAction;
    private final String font;

    public ReplacementToken(String originalContent, String content, String hover, String click, ClickEvent.Action clickAction, String font) {
        super(originalContent);

        this.content = content;
        this.hover = hover;
        this.click = click;
        this.clickAction = clickAction;
        this.font = font;
    }

    public ReplacementToken(String originalContent, String content, String hover, String font) {
        this(originalContent, content, hover, null, null, font);
    }

    public ReplacementToken(String originalContent, String content) {
        this(originalContent, content, null, null, null, "default");
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

    @Override
    public String getFont() {
        return this.font;
    }
}

package dev.rosewood.rosechat.message.wrapper.tokenizer;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;

public class GenericToken extends Token {

    private final String content;
    private final String hover;
    private final String click;
    private final HoverEvent.Action hoverAction;
    private final ClickEvent.Action clickAction;
    private final String font;
    private final boolean requiresTokenizing;

    public GenericToken(String originalContent, String content, String hover, HoverEvent.Action hoverAction, String click, ClickEvent.Action clickAction, String font, boolean tokenizeRecursively) {
        super(originalContent);

        this.content = content;
        this.hover = hover;
        this.hoverAction = hoverAction;
        this.click = click;
        this.clickAction = clickAction;
        this.font = font;
        this.requiresTokenizing = tokenizeRecursively;
    }

    public GenericToken(String originalContent, String content, String hover, HoverEvent.Action hoverAction, String click, ClickEvent.Action clickAction, String font) {
        this(originalContent, content, hover, hoverAction, click, clickAction, font, true);
    }

    public GenericToken(String originalContent, String content, String hover, HoverEvent.Action hoverAction, String click, ClickEvent.Action clickAction) {
        this(originalContent, content, hover, hoverAction, click, clickAction, "default", true);
    }

    public GenericToken(String originalContent, String content, String hover, HoverEvent.Action hoverAction, String font) {
        this(originalContent, content, hover, hoverAction, null, null, font, true);
    }

    public GenericToken(String originalContent, String content, String hover, HoverEvent.Action hoverAction) {
        this(originalContent, content, hover, hoverAction, null, null, "default", true);
    }

    public GenericToken(String originalContent, String content, String hover, String font) {
        this(originalContent, content, hover, HoverEvent.Action.SHOW_TEXT, null, null, font, true);
    }

    public GenericToken(String originalContent, String content, String hover) {
        this(originalContent, content, hover, HoverEvent.Action.SHOW_TEXT, null, null, "default", true);
    }

    public GenericToken(String orignalContent, String content) {
        this(orignalContent, content, null, null, null, null, "default", true);
    }

    public GenericToken(String originalContent) {
        this(originalContent, originalContent, null, null, null, null, "default", true);
    }

    @Override
    public String getContent() {
        return this.content;
    }

    @Override
    public boolean requiresTokenizing() {
        return this.requiresTokenizing;
    }

    @Override
    public String getHover() {
        return this.hover;
    }

    @Override
    public HoverEvent.Action getHoverAction() {
        return this.hoverAction;
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

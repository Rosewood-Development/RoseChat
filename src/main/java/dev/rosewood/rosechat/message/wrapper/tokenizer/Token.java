package dev.rosewood.rosechat.message.wrapper.tokenizer;

import dev.rosewood.rosegarden.utils.HexUtils;
import java.util.ArrayList;
import java.util.List;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;

public class Token {

    protected final String originalContent;
    protected String content;
    protected String hover;
    protected String click;
    protected String font;
    protected HoverEvent.Action hoverAction;
    protected ClickEvent.Action clickAction;
    protected boolean requiresTokenizing;
    protected List<Token> children;

    public Token(String originalContent, String content, String hover, HoverEvent.Action hoverAction, String click, ClickEvent.Action clickAction, String font, boolean tokenizeRecursively) {
        this.originalContent = originalContent;
        this.font = font;
        this.content = content;
        this.hover = hover;
        this.hoverAction = hoverAction;
        this.click = click;
        this.clickAction = clickAction;
        this.requiresTokenizing = tokenizeRecursively;
        this.children = new ArrayList<>();
    }

    public Token(String originalContent, String content, String hover, HoverEvent.Action hoverAction, String click, ClickEvent.Action clickAction, String font) {
        this(originalContent, content, hover, hoverAction, click, clickAction, font, true);
    }

    public Token(String originalContent, String content, String hover, HoverEvent.Action hoverAction, String click, ClickEvent.Action clickAction) {
        this(originalContent, content, hover, hoverAction, click, clickAction, null, true);
    }

    public Token(String originalContent, String content, String hover, HoverEvent.Action hoverAction, String font) {
        this(originalContent, content, hover, hoverAction, null, null, font, true);
    }

    public Token(String originalContent, String content, String hover, HoverEvent.Action hoverAction) {
        this(originalContent, content, hover, hoverAction, null, null, null, true);
    }

    public Token(String originalContent, String content, String hover, String font) {
        this(originalContent, content, hover, HoverEvent.Action.SHOW_TEXT, null, null, font, true);
    }

    public Token(String originalContent, String content, String hover) {
        this(originalContent, content, hover, HoverEvent.Action.SHOW_TEXT, null, null, null, true);
    }

    public Token(String originalContent, String content) {
        this(originalContent, content, null, null, null, null, null, true);
        this.content = content;
    }

    public Token(String originalContent) {
        this(originalContent, originalContent, null, null, null, null, null, true);
    }

    public final String getOriginalContent() {
        return this.originalContent;
    }

    public String getContent() {
        return this.content;
    }

    public boolean requiresTokenizing() {
        return this.requiresTokenizing;
    }

    public void addChildren(List<Token> children) {
        Bukkit.broadcastMessage(this.font + " / " + this.hover + " / " + this.click + " / " + children.size());
        if (this.font != null) children.stream().filter(x -> x.font == null).forEach(child -> child.font = this.font);
        if (this.hover != null) children.stream().filter(x -> x.hover == null).forEach(child -> {
            child.hover = this.hover;
            child.hoverAction = this.hoverAction;
        });
        if (this.click != null) children.stream().filter(x -> x.click == null).forEach(child -> {
            child.click = this.click;
            child.clickAction = this.clickAction;
        });
        this.children.addAll(children);
    }

    public List<Token> getChildren() {
        return this.children;
    }

    public String getHover() {
        return this.hover;
    }

    public HoverEvent.Action getHoverAction() {
        return this.hover == null ? null : (this.hoverAction == null ? HoverEvent.Action.SHOW_TEXT : this.hoverAction);
    }

    public String getClick() {
        return this.click;
    }

    public ClickEvent.Action getClickAction() {
        return this.click == null ? null : (this.clickAction == null ? ClickEvent.Action.SUGGEST_COMMAND : this.clickAction);
    }

    public HexUtils.ColorGenerator getColorGenerator(List<Token> futureTokens) {
        return null;
    }

    protected int getColorGeneratorContentLength(List<Token> futureTokens) {
        int contentLength = 0;
        for (Token token : futureTokens) {
            if (!token.hasColorGenerator() || token == this) {
                if (token.getChildren().isEmpty()) {
                    contentLength += token.getContent().length();
                } else if (token.getChildren().stream().noneMatch(x -> x.getColorGenerator(futureTokens) != null)) {
                    contentLength += token.getChildren().stream().mapToInt(x -> x.getContent().length()).sum();
                }
            } else break;
        }
        return contentLength;
    }

    public boolean hasColorGenerator() {
        return false;
    }

}

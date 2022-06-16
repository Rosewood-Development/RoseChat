package dev.rosewood.rosechat.message.wrapper.tokenizer;

import dev.rosewood.rosegarden.utils.HexUtils;
import java.util.ArrayList;
import java.util.List;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;

public abstract class Token {

    protected final String originalContent;
    protected String font;
    protected List<Token> children;

    public Token(String originalContent) {
        this.originalContent = originalContent;
        this.font = "default";
        this.children = new ArrayList<>();
    }

    public final String getOriginalContent() {
        return this.originalContent;
    }

    public abstract String getContent();

    public boolean requiresTokenizing() {
        return false;
    }

    public void addChildren(List<Token> children) {
        this.children.addAll(children);
    }

    public List<Token> getChildren() {
        return this.children;
    }

    public String getHover() {
        return null;
    }

    public HoverEvent.Action getHoverAction() {
        return HoverEvent.Action.SHOW_TEXT;
    }

    public String getClick() {
        return null;
    }

    public ClickEvent.Action getClickAction() {
        return null;
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

    public String getFont() {
        return this.font;
    }

    public void setFont(String font) {
        this.font = font;
    }

}

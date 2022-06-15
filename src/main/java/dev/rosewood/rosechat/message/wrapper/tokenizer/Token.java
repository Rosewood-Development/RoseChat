package dev.rosewood.rosechat.message.wrapper.tokenizer;

import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosegarden.utils.HexUtils;
import java.util.ArrayList;
import java.util.List;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;

public abstract class Token {

    // TODO: Add MessageWrapper and RoseSender (viewer) here and remove from methods
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

    public abstract String getContent(MessageWrapper wrapper, RoseSender viewer);

    public boolean requiresTokenizing() {
        return false;
    }

    public void addChildren(List<Token> children) {
        this.children.addAll(children);
    }

    public List<Token> getChildren() {
        return this.children;
    }

    public String getHover(MessageWrapper wrapper, RoseSender viewer) {
        return null;
    }

    public HoverEvent.Action getHoverAction(MessageWrapper wrapper, RoseSender viewer) {
        return HoverEvent.Action.SHOW_TEXT;
    }

    public String getClick(MessageWrapper wrapper, RoseSender viewer) {
        return null;
    }

    public ClickEvent.Action getClickAction(MessageWrapper wrapper, RoseSender viewer) {
        return ClickEvent.Action.RUN_COMMAND;
    }

    public HexUtils.ColorGenerator getColorGenerator(MessageWrapper wrapper, RoseSender viewer, List<Token> futureTokens) {
        return null;
    }

    protected int getColorGeneratorContentLength(MessageWrapper wrapper, RoseSender viewer, List<Token> futureTokens) {
        int contentLength = 0;
        for (Token token : futureTokens) {
            if (!token.hasColorGenerator() || token == this) {
                if (token.getChildren().isEmpty()) {
                    contentLength += token.getContent(wrapper, viewer).length();
                } else if (token.getChildren().stream().noneMatch(x -> x.getColorGenerator(wrapper, viewer, futureTokens) != null)) {
                    contentLength += token.getChildren().stream().mapToInt(x -> x.getContent(wrapper, viewer).length()).sum();
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

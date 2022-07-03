package dev.rosewood.rosechat.message.wrapper.tokenizer;

import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;

public class Token {

    protected final String originalContent;
    protected String content;
    protected String hover;
    protected String click;
    protected String font;
    protected HoverEvent.Action hoverAction;
    protected ClickEvent.Action clickAction;
    protected boolean requiresTokenizing;
    protected Set<Tokenizer<?>> ignoredTokenizers;
    protected StringPlaceholders.Builder placeholders;
    protected List<Token> children, hoverChildren;

    public Token(TokenSettings settings) {
        this.originalContent = settings.originalContent;
        this.content = settings.content;
        this.hover = settings.hover;
        this.click = settings.click;
        this.font = settings.font;
        this.hoverAction = settings.hoverAction;
        this.clickAction = settings.clickAction;
        this.requiresTokenizing = settings.requiresTokenizing;
        this.ignoredTokenizers = settings.ignoredTokenizers;
        this.placeholders = settings.placeholders;
        this.children = new ArrayList<>();
        this.hoverChildren = new ArrayList<>();
    }

    public final String getOriginalContent() {
        return this.originalContent;
    }

    public String getContent() {
        return this.placeholders.build().apply(this.content);
    }

    public boolean requiresTokenizing() {
        return this.requiresTokenizing;
    }

    public void addChildren(List<Token> children) {
        for (Token child : children) {
            if (this.font != null) {
                child.font = this.font;
            }

            if (this.hover != null) {
                child.hover = this.getHover();
                child.hoverChildren = this.hoverChildren;
                child.hoverAction = this.hoverAction;
            }

            if (this.click != null) {
                child.click = this.getClick();
                child.clickAction = this.clickAction;
            }
        }
        this.children.addAll(children);
    }

    public List<Token> getChildren() {
        return this.children;
    }

    public void addHoverChildren(List<Token> children) {
        this.hoverChildren.addAll(children);
    }

    public String getHover() {
        return this.hover == null ? null : this.placeholders.build().apply(this.hover);
    }

    public HoverEvent.Action getHoverAction() {
        return this.hover == null ? null : (this.hoverAction == null ? HoverEvent.Action.SHOW_TEXT : this.hoverAction);
    }

    public List<Token> getHoverChildren() {
        return this.hoverChildren;
    }

    public String getClick() {
        if (this.click == null)
            return null;

        if (this.getClickAction() == ClickEvent.Action.OPEN_URL && !this.click.startsWith("http://") && !this.click.startsWith("https://")) {
            return this.placeholders.build().apply("https://" + this.click);
        } else {
            return this.placeholders.build().apply(this.click);
        }
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

    public FormattedColorGenerator applyFormatCodes(FormattedColorGenerator colorGenerator, String baseColor, List<Token> futureTokens) {
        return null;
    }

    public boolean hasFormatCodes() {
        return false;
    }

    public String getEffectiveFont() {
        return this.font == null ? "default" : this.font;
    }

    public Set<Tokenizer<?>> getIgnoredTokenizers() {
        return this.ignoredTokenizers;
    }

    public StringPlaceholders getPlaceholders() {
        return this.placeholders.build();
    }

    public static class TokenSettings {

        private final String originalContent;
        private String content, hover, click, font;
        private HoverEvent.Action hoverAction;
        private ClickEvent.Action clickAction;
        private boolean requiresTokenizing;
        private final Set<Tokenizer<?>> ignoredTokenizers;
        private final StringPlaceholders.Builder placeholders;

        public TokenSettings(String originalContent) {
            this.originalContent = originalContent;
            this.content = originalContent;
            this.hover = null;
            this.click = null;
            this.font = null;
            this.hoverAction = null;
            this.clickAction = null;
            this.requiresTokenizing = true;
            this.ignoredTokenizers = new HashSet<>();
            this.placeholders = StringPlaceholders.builder();
        }

        public TokenSettings content(String text) {
            this.content = text;
            return this;
        }

        public TokenSettings hover(String text) {
            this.hover = text;
            return this;
        }

        public TokenSettings click(String text) {
            this.click = text;
            return this;
        }

        public TokenSettings font(String font) {
            this.font = font;
            return this;
        }

        public TokenSettings hoverAction(HoverEvent.Action action) {
            this.hoverAction = action;
            return this;
        }

        public TokenSettings clickAction(ClickEvent.Action action) {
            this.clickAction = action;
            return this;
        }

        public TokenSettings requiresTokenizing(boolean tokenizeRecursively) {
            this.requiresTokenizing = tokenizeRecursively;
            return this;
        }

        public TokenSettings ignoreTokenizer(Tokenizer<?> tokenizer) {
            this.ignoredTokenizers.add(tokenizer);
            return this;
        }

        public TokenSettings placeholder(String placeholder, Object value) {
            this.placeholders.addPlaceholder(placeholder, value);
            return this;
        }

    }

}

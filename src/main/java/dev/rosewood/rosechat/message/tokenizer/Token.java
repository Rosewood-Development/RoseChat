package dev.rosewood.rosechat.message.tokenizer;

import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import java.util.Objects;
import java.util.stream.Collectors;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Token implements Cloneable {

    protected final String originalContent;
    protected String content;
    protected String hover;
    protected String click;
    protected String font;
    protected HoverEvent.Action hoverAction;
    protected ClickEvent.Action clickAction;
    protected boolean requiresTokenizing;
    protected boolean retainColour;
    protected Set<Tokenizer<?>> ignoredTokenizers;
    protected StringPlaceholders.Builder placeholders;
    protected List<Token> children, hoverChildren;
    protected boolean allowsCaching;

    /**
     * Creates a new token with the given settings.
     * @param settings The {@link TokenSettings} to use.
     */
    public Token(TokenSettings settings) {
        this.originalContent = settings.originalContent;
        this.content = settings.content;
        this.hover = settings.hover;
        this.click = settings.click;
        this.font = settings.font;
        this.hoverAction = settings.hoverAction;
        this.clickAction = settings.clickAction;
        this.requiresTokenizing = settings.requiresTokenizing;
        this.retainColour = settings.retainColour;
        this.ignoredTokenizers = settings.ignoredTokenizers;
        this.placeholders = settings.placeholders;
        this.allowsCaching = settings.allowsCaching;
        this.children = new ArrayList<>();
        this.hoverChildren = new ArrayList<>();
    }

    /**
     * @return The original text before any changes.
     */
    public final String getOriginalContent() {
        return this.originalContent;
    }

    /**
     * @return The final text after changes are applied.
     */
    public String getContent() {
        return this.placeholders.build().apply(this.content);
    }

    /**
     * @return Whether this token can be tokenized.
     */
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

            child.retainColour = this.retainColour;
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

        String effectiveClick = this.placeholders.build().apply(this.click);
        if (this.getClickAction() == ClickEvent.Action.OPEN_URL && !effectiveClick.startsWith("http")) {
            return "https://" + effectiveClick;
        } else {
            return effectiveClick;
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
                    contentLength += token.getContent().replaceAll("\\s+", "").length();
                } else if (token.getChildren().stream().noneMatch(x -> x.getColorGenerator(futureTokens) != null)) {
                    contentLength += token.getChildren().stream().mapToInt(x -> x.getContent().replaceAll("\\s+", "").length()).sum();
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

    public boolean allowsCaching() {
        return this.allowsCaching
                && this.children.stream().allMatch(Token::allowsCaching)
                && this.hoverChildren.stream().allMatch(Token::allowsCaching);
    }

    public String getEffectiveFont() {
        return this.font == null ? "default" : this.font;
    }

    public Set<Tokenizer<?>> getIgnoredTokenizers() {
        return this.ignoredTokenizers;
    }

    public boolean shouldRetainColour() {
        return this.retainColour;
    }

    public StringPlaceholders getPlaceholders() {
        return this.placeholders.build();
    }

    public void applyInheritance(Token child) {
        child.ignoredTokenizers.addAll(this.ignoredTokenizers);
        child.placeholders.addAll(this.getPlaceholders());
        child.retainColour = this.retainColour;

        if (this.hover != null) {
            child.hover = this.getHover();
            child.hoverAction = this.hoverAction;
        }

        if (this.click != null) {
            child.click = this.getClick();
            child.clickAction = this.clickAction;
        }
    }

    @Override
    public Token clone() {
        try {
            Token clone = (Token) super.clone();
            clone.ignoredTokenizers = new HashSet<>(this.ignoredTokenizers);
            clone.children = this.children.stream().map(Token::clone).collect(Collectors.toList());
            clone.hoverChildren = this.hoverChildren.stream().map(Token::clone).collect(Collectors.toList());
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Token token = (Token) o;
        return this.requiresTokenizing == token.requiresTokenizing
                && this.retainColour == token.retainColour
                && this.allowsCaching == token.allowsCaching
                && Objects.equals(this.hover, token.hover)
                && Objects.equals(this.click, token.click)
                && Objects.equals(this.font, token.font)
                && this.hoverAction == token.hoverAction
                && this.clickAction == token.clickAction
                && this.ignoredTokenizers.equals(token.ignoredTokenizers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.hover, this.click, this.font, this.hoverAction, this.clickAction, this.requiresTokenizing, this.retainColour, this.ignoredTokenizers, this.allowsCaching);
    }

    public static class TokenSettings {

        private final String originalContent;
        private String content, hover, click, font;
        private HoverEvent.Action hoverAction;
        private ClickEvent.Action clickAction;
        private boolean requiresTokenizing;
        private boolean retainColour;
        private final Set<Tokenizer<?>> ignoredTokenizers;
        private final StringPlaceholders.Builder placeholders;
        private boolean allowsCaching;

        public TokenSettings(String originalContent) {
            this.originalContent = originalContent;
            this.content = originalContent;
            this.hover = null;
            this.click = null;
            this.font = null;
            this.hoverAction = null;
            this.clickAction = null;
            this.requiresTokenizing = true;
            this.retainColour = false;
            this.allowsCaching = true;
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

        public TokenSettings retainColour(boolean retainColour) {
            this.retainColour = retainColour;
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

        public TokenSettings noCaching() {
            this.allowsCaching = false;
            return this;
        }

    }

}

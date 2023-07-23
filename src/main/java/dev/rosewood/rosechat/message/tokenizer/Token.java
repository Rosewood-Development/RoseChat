package dev.rosewood.rosechat.message.tokenizer;

import dev.rosewood.rosechat.message.tokenizer.decorator.TokenDecorator;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Token {

    protected Token parent; // Exposed and set in the MessageTokenizer class
    private final List<Token> children;
    private final TokenType type;
    private final String content;
    private final List<TokenDecorator> decorators;
    private final boolean containsPlayerInput;
    private final StringPlaceholders placeholders;
    private final boolean encapsulate;
    private final Set<Tokenizer> ignoredTokenizers;

    private Token(TokenType type, String content, List<TokenDecorator> decorators, boolean containsPlayerInput,
                  StringPlaceholders placeholders, boolean encapsulate, Set<Tokenizer> ignoredTokenizers) {
        this.type = type;
        this.content = content;
        this.children = new ArrayList<>();
        this.decorators = decorators;
        this.containsPlayerInput = containsPlayerInput;
        this.placeholders = placeholders;
        this.encapsulate = encapsulate;
        this.ignoredTokenizers = ignoredTokenizers;
    }

    /**
     * @return the type of this token
     */
    public TokenType getType() {
        return this.type;
    }

    /**
     * Gets the content of this token.
     *
     * @return the content of this token
     * @throws IllegalStateException if this token has a type of {@link TokenType#DECORATOR}
     */
    public String getContent() {
        if (this.type == TokenType.DECORATOR)
            throw new IllegalStateException("Cannot get content of a token that is of type DECORATOR");
        return this.getPlaceholders().apply(this.content);
    }

    /**
     * Gets the children of this token if it is of type {@link TokenType#GROUP}.
     *
     * @return the effective children of this token
     * @throws IllegalStateException if this token is not of type {@link TokenType#GROUP}
     */
    public List<Token> getChildren() {
        if (this.type != TokenType.GROUP)
            throw new IllegalStateException("Cannot get children of a token that is not of type GROUP");
        return this.children;
    }

    /**
     * @return the decorators to be applied to this token
     */
    public List<TokenDecorator> getDecorators() {
        return this.decorators;
    }

    /**
     * @return true if this token contains player input, false otherwise
     */
    public boolean containsPlayerInput() {
        return this.containsPlayerInput || (this.parent != null && this.parent.containsPlayerInput());
    }

    /**
     * @return true if this token has no decorators, false otherwise
     */
    public boolean isPlain() {
        return this.decorators.isEmpty();
    }

    /**
     * @return true if this token should use an encapsulated decorator context, false otherwise
     */
    public boolean shouldEncapsulate() {
        return this.encapsulate;
    }

    /**
     * Checks if a Tokenizer should be ignored.
     *
     * @param tokenizer The Tokenizer to check
     * @return true if the Tokenizer should be ignored, false otherwise
     */
    public boolean ignoresTokenizer(Tokenizer tokenizer) {
        return this.ignoredTokenizers.contains(tokenizer);
    }

    /**
     * @return the ignored tokenizers
     */
    public Set<Tokenizer> getIgnoredTokenizers() {
        return Collections.unmodifiableSet(this.ignoredTokenizers);
    }

    public StringPlaceholders getPlaceholders() {
        StringPlaceholders.Builder builder = StringPlaceholders.builder();
        builder.addAll(this.placeholders);
        if (this.parent != null)
            builder.addAll(this.parent.getPlaceholders());
        return builder.build();
    }

    public static Builder text(String value) {
        return new Builder(TokenType.TEXT, value);
    }

    public static Builder group(String rawContent) {
        return new Builder(TokenType.GROUP, rawContent);
    }

    public static Builder decorator(TokenDecorator decorator) {
        return new Builder(TokenType.DECORATOR, null).decorate(decorator);
    }

    public static class Builder {

        private final TokenType tokenType;
        private final String content;
        private final List<TokenDecorator> decorators;
        private boolean containsPlayerInput;
        private StringPlaceholders.Builder placeholders;
        private boolean encapsulate;
        private Set<Tokenizer> ignoredTokenizers;

        private Builder(TokenType tokenType, String content) {
            this.tokenType = tokenType;
            this.content = content;
            this.decorators = new ArrayList<>();
            this.containsPlayerInput = false;
        }

        public Builder decorate(TokenDecorator decorator) {
            this.decorators.add(decorator);
            return this;
        }

        public Builder containsPlayerInput() {
            this.containsPlayerInput = true;
            return this;
        }

        public Builder placeholder(String placeholder, Object value) {
            if (this.placeholders == null)
                this.placeholders = StringPlaceholders.builder();
            this.placeholders.add(placeholder, value);
            return this;
        }

        public Builder placeholders(StringPlaceholders placeholders) {
            if (this.placeholders == null)
                this.placeholders = StringPlaceholders.builder();
            this.placeholders.addAll(placeholders);
            return this;
        }

        public Builder encapsulate() {
            this.encapsulate = true;
            return this;
        }

        public Builder ignoreTokenizer(Tokenizer tokenizer) {
            if (this.ignoredTokenizers == null)
                this.ignoredTokenizers = new HashSet<>();
            this.ignoredTokenizers.add(tokenizer);
            return this;
        }

        public Token build() {
            return new Token(
                    this.tokenType,
                    this.content,
                    this.decorators,
                    this.containsPlayerInput,
                    this.placeholders == null ? StringPlaceholders.empty() : this.placeholders.build(),
                    this.encapsulate,
                    this.ignoredTokenizers == null ? Set.of() : this.ignoredTokenizers
            );
        }

    }

}

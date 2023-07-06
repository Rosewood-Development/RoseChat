package dev.rosewood.rosechat.message.tokenizer;

import dev.rosewood.rosechat.message.tokenizer.decorator.TokenDecorator;
import java.util.ArrayList;
import java.util.List;

public class Token {

    protected Token parent; // Exposed and set in the MessageTokenizer class
    protected final List<Token> children; // Exposed and added to in the MessageTokenizer class
    private final TokenType type;
    private final String content;
    private final List<TokenDecorator> decorators;
    private final boolean containsPlayerInput;

    private Token(TokenType type, String content, List<TokenDecorator> decorators, boolean containsPlayerInput) {
        this.type = type;
        this.content = content;
        this.children = new ArrayList<>();
        this.decorators = decorators;
        this.containsPlayerInput = containsPlayerInput;
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
            throw new IllegalStateException("Cannot get content of a decorator token");
        return this.content;
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

    public static Builder text(String value) {
        return new Builder(TokenType.TEXT, value);
    }

    public static Builder group(String rawContent) {
        return new Builder(TokenType.GROUP, rawContent);
    }

    public static Builder decorator() {
        return new Builder(TokenType.DECORATOR, null);
    }

    public static class Builder {

        private final TokenType tokenType;
        private String content;
        private final List<TokenDecorator> decorators;
        private boolean containsPlayerInput;

        private Builder(TokenType tokenType, String content) {
            this.tokenType = tokenType;
            this.content = content;
            this.decorators = new ArrayList<>();
            this.containsPlayerInput = false;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder addDecorator(TokenDecorator decorator) {
            this.decorators.add(decorator);
            return this;
        }

        public Builder containsPlayerInput() {
            this.containsPlayerInput = true;
            return this;
        }

        public Token build() {
            return new Token(this.tokenType, this.content, this.decorators, this.containsPlayerInput);
        }

    }

}

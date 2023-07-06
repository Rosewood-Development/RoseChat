package dev.rosewood.rosechat.message.tokenizer;

import dev.rosewood.rosechat.message.tokenizer.decorator.TokenDecorator;
import java.util.ArrayList;
import java.util.List;

public class Token {

    protected Token parent; // Exposed and set in the MessageTokenizer class
    private TokenType type;
    private String content;
    private final List<Token> children;
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

    protected void setChildren(List<Token> children) {
        if (this.type != TokenType.GROUP)
            throw new IllegalStateException("Cannot set children of a token that is not of type GROUP");

        // Combine all children into a single token if they are all text and have no decorators and transform this into a single text token instead
        if (children.stream().allMatch(x -> x.type == TokenType.TEXT && x.isPlain())) {
            StringBuilder builder = new StringBuilder();
            for (Token child : children)
                builder.append(child.getContent());

            this.content = builder.toString();
            this.type = TokenType.TEXT;
            return;
        }

        // If all children are decorators combine them into a single list and make this a decorator token
        if (children.stream().allMatch(x -> x.type == TokenType.DECORATOR)) {
            List<TokenDecorator> decorators = new ArrayList<>();
            for (Token child : children)
                decorators.addAll(child.getDecorators());

            this.type = TokenType.DECORATOR;
            this.decorators.addAll(decorators);
            return;
        }

        this.children.clear();
        this.children.addAll(children);
    }

    /**
     * Token decorators are applied differently depending on the state of this token:
     * This token has no children:
     *  - If this token's content is not empty, the decorators are applied to this token
     *  - If this token's content is empty, the decorators are applied to the parent token's context
     * This token has children:
     *  - The decorators are applied to the children of this token
     *
     * @return the decorators belonging to this token
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
    private boolean isPlain() {
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

package dev.rosewood.rosechat.message.tokenizer;

import java.util.ArrayList;
import java.util.List;

public class Token {

    private final String content;
    private final List<Token> children;
    private final List<TokenDecorator> decorators;
    private final boolean containsPlayerInput;

    private Token(String content, List<Token> children, List<TokenDecorator> decorators, boolean containsPlayerInput) {
        this.content = content;
        this.children = children;
        this.decorators = decorators;
        this.containsPlayerInput = containsPlayerInput;
    }

    public String getContent() {
        return this.content;
    }

    public void addChild(Token token) {
        this.children.add(token);
    }

    public List<Token> getChildren() {
        return this.children;
    }

    public boolean isResolved() {
        return this.children.isEmpty();
    }

    public boolean containsPlayerInput() {
        return this.containsPlayerInput;
    }

    public List<TokenDecorator> getDecorators() {
        return this.decorators;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String content;
        private final List<Token> children;
        private final List<TokenDecorator> decorators;
        private boolean containsPlayerInput;

        private Builder() {
            this.content = "";
            this.children = new ArrayList<>();
            this.decorators = new ArrayList<>();
            this.containsPlayerInput = false;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder containsPlayerInput(boolean containsPlayerInput) {
            this.containsPlayerInput = containsPlayerInput;
            return this;
        }

        public Builder addDecorator(TokenDecorator decorator) {
            this.decorators.add(decorator);
            return this;
        }

        public Token build() {
            return new Token(this.content, this.children, this.decorators, this.containsPlayerInput);
        }

    }

}

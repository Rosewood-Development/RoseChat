package dev.rosewood.rosechat.message.tokenizer;

import java.util.ArrayList;
import java.util.List;

public class Token {

    private final String content;
    private final List<Token> children;
    private final boolean resolved;
    private final List<TokenDecorator> decorators;
    private final boolean containsPlayerInput;

    private Token(String content, boolean resolved, List<TokenDecorator> decorators, boolean containsPlayerInput) {
        this.content = content;
        this.children = new ArrayList<>();
        this.resolved = resolved;
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
        return this.resolved;
    }

    public List<TokenDecorator> getDecorators() {
        return this.decorators;
    }

    public boolean containsPlayerInput() {
        return this.containsPlayerInput;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String content;
        private boolean resolved;
        private final List<TokenDecorator> decorators;
        private boolean containsPlayerInput;

        private Builder() {
            this.content = "";
            this.resolved = false;
            this.decorators = new ArrayList<>();
            this.containsPlayerInput = false;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder resolve() {
            this.resolved = true;
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
            return new Token(this.content, this.resolved, this.decorators, this.containsPlayerInput);
        }

    }

}

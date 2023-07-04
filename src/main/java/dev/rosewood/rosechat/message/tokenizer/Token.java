package dev.rosewood.rosechat.message.tokenizer;

import dev.rosewood.rosechat.message.tokenizer.decorator.TokenDecorator;
import java.util.ArrayList;
import java.util.List;

public class Token {

    protected Token parent; // Exposed and set in the MessageTokenizer class
    protected final List<Token> children; // Exposed and set in the MessageTokenizer class
    private final String content;
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

    /**
     * Gets the content of this token.
     * If {@link #isResolved()} is true, this will be the final content of the token.
     * If {@link #isResolved()} is false, this will be the raw content of the token.
     *
     * @return the content of this token
     */
    public String getContent() {
        return this.content;
    }

    /**
     * @return true if this token has content, false otherwise
     */
    public boolean hasContent() {
        return !this.content.isEmpty();
    }

    /**
     * @return the effective children of this token
     */
    public List<Token> getChildren() {
//        // Combine all children into a single token if they are all resolved and have no immediate decorators
//        boolean allResolved = true;
//        boolean allDecoratorsEmpty = true;
//        for (Token child : this.children) {
//            if (!child.isResolved())
//                allResolved = false;
//            if (!child.decorators.isEmpty())
//                allDecoratorsEmpty = false;
//        }
//
//        if (allResolved && allDecoratorsEmpty) {
//            StringBuilder combinedContent = new StringBuilder();
//            for (Token child : this.children)
//                combinedContent.append(child.getContent());
//            return Collections.singletonList(Token.builder().content(combinedContent.toString()).resolve().build());
//        }

        return this.children;
    }

    /**
     * @return true if this token has children, false otherwise
     */
    public boolean hasChildren() {
        return !this.children.isEmpty();
    }

    /**
     * This should NOT be used to determine how the Token is displayed.
     *
     * @return true if this token's content will be tokenized again, false otherwise
     */
    public boolean isResolved() {
        return this.resolved;
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

    public boolean hasDecorators() {
        return !this.decorators.isEmpty();
    }

    /**
     * @return true if this token contains player input, false otherwise
     */
    public boolean containsPlayerInput() {
        return this.containsPlayerInput || (this.parent != null && this.parent.containsPlayerInput());
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

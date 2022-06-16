package dev.rosewood.rosechat.message.wrapper.tokenizer.placeholder.papi;

import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;

public class PAPIPlaceholderToken extends Token {

    private final String content;

    public PAPIPlaceholderToken(String originalContent, String content) {
        super(originalContent);

        this.content = content;
    }

    @Override
    public String getContent() {
        return this.content;
    }

    @Override
    public boolean requiresTokenizing() {
        return true;
    }

}

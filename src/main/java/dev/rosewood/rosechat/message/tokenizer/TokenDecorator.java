package dev.rosewood.rosechat.message.tokenizer;

public interface TokenDecorator {

    TokenDecorator EMPTY = token -> { };

    void apply(Token token);

}

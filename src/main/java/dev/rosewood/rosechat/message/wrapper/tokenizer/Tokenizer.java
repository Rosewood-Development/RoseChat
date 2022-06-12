package dev.rosewood.rosechat.message.wrapper.tokenizer;

import dev.rosewood.rosechat.message.MessageWrapper;

public interface Tokenizer<T extends Token> {

    T tokenize(MessageWrapper messageWrapper, String input);
    
}

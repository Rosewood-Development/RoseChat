package dev.rosewood.rosechat.message.wrapper.tokenizer;

import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;

public interface Tokenizer<T extends Token> {

    T tokenize(MessageWrapper messageWrapper, RoseSender viewer, String input, boolean ignorePermissions);
    
}

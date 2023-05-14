package dev.rosewood.rosechat.message.tokenizer;

import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosechat.message.RosePlayer;

public interface Tokenizer<T extends Token> {

    T tokenize(RoseMessage roseMessage, RosePlayer viewer, String input, boolean ignorePermissions);

}

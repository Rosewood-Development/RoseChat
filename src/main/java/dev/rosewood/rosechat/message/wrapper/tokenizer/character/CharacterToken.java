package dev.rosewood.rosechat.message.wrapper.tokenizer.character;

import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;

public class CharacterToken extends Token {

    public CharacterToken(String originalContent) {
        super(originalContent);
    }
    
    @Override
    public String getText(MessageWrapper wrapper) {
        return this.getOriginalContent();
    }
}

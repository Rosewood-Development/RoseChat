package dev.rosewood.rosechat.message.wrapper.tokenizer.character;

import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;

public class CharacterToken extends Token {

    public CharacterToken(RoseSender sender, RoseSender viewer, String originalContent) {
        super(sender, viewer, originalContent);
    }
}

package dev.rosewood.rosechat.message.wrapper.tokenizer.whitespace;

import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;

public class WhitespaceToken extends Token {

    public WhitespaceToken(RoseSender sender, RoseSender viewer, String originalContent) {
        super(sender, viewer, originalContent);
    }
}

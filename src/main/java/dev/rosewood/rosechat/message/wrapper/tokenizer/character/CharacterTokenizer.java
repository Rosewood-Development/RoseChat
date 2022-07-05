package dev.rosewood.rosechat.message.wrapper.tokenizer.character;

import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;

public class CharacterTokenizer implements Tokenizer<Token> {

    @Override
    public Token tokenize(MessageWrapper messageWrapper, RoseSender viewer, String input, boolean ignorePermissions) {
        return new Token(new Token.TokenSettings(String.valueOf(input.charAt(0))).requiresTokenizing(false));
    }

}

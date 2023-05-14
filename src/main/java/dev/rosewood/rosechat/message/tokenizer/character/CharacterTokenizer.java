package dev.rosewood.rosechat.message.tokenizer.character;

import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;

public class CharacterTokenizer implements Tokenizer<Token> {

    @Override
    public Token tokenize(RoseMessage roseMessage, RosePlayer viewer, String input, boolean ignorePermissions) {
        return new Token(new Token.TokenSettings(String.valueOf(input.charAt(0))).requiresTokenizing(false));
    }

}

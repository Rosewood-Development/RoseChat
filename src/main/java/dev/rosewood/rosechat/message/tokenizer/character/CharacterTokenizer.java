package dev.rosewood.rosechat.message.tokenizer.character;

import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;

public class CharacterTokenizer implements Tokenizer {

    @Override
    public TokenizerResult tokenize(TokenizerParams params) {
        return new TokenizerResult(Token.builder().content(String.valueOf(params.getInput().charAt(0))).build(), 1);
    }

}

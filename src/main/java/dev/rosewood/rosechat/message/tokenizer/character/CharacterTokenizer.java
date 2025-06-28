package dev.rosewood.rosechat.message.tokenizer.character;

import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;

public class CharacterTokenizer extends Tokenizer {

    public CharacterTokenizer() {
        super("character");
    }

    @Override
    public TokenizerResult tokenize(TokenizerParams params) {
        return new TokenizerResult(Token.text(String.valueOf(params.getInput().charAt(0))), 1);
    }

}

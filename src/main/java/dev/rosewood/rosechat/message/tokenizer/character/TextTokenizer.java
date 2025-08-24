package dev.rosewood.rosechat.message.tokenizer.character;

import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import java.util.List;

public class TextTokenizer extends Tokenizer {

    public TextTokenizer() {
        super("text");
    }

    @Override
    public List<TokenizerResult> tokenize(TokenizerParams params) {
        return List.of(new TokenizerResult(Token.text(params.getInput()), 0, params.getInput().length()));
    }

}

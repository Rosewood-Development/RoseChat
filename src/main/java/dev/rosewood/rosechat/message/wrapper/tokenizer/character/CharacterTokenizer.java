package dev.rosewood.rosechat.message.wrapper.tokenizer.character;

import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;

public class CharacterTokenizer implements Tokenizer<CharacterToken> {

    @Override
    public CharacterToken tokenize(MessageWrapper messageWrapper, RoseSender viewer, String input) {
        return new CharacterToken(String.valueOf(input.charAt(0)));
    }

}

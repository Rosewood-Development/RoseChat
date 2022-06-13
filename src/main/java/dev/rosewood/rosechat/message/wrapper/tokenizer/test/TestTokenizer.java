package dev.rosewood.rosechat.message.wrapper.tokenizer.test;

import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;

public class TestTokenizer implements Tokenizer<TestToken> {
    
    @Override
    public TestToken tokenize(MessageWrapper messageWrapper, String input) {
        if (input.startsWith("["))
            return new TestToken(input.substring(0, input.indexOf("]")));
        return null;
    }

}

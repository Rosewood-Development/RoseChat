package dev.rosewood.rosechat.message.contents;

import dev.rosewood.rosechat.message.tokenizer.MessageOutputs;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.composer.ChatComposer;

class RoseChatMessageContents extends MessageContents {

    private final Token token;

    public RoseChatMessageContents(Token token, MessageOutputs outputs) {
        super(outputs);
        this.token = token;
    }

    @Override
    protected <T> T compose(ChatComposer<T> composer) {
        return composer.compose(this.token);
    }

}

package dev.rosewood.rosechat.message.contents;

import dev.rosewood.rosechat.message.tokenizer.MessageOutputs;
import dev.rosewood.rosechat.message.tokenizer.composer.ChatComposer;

class JsonMessageContents extends MessageContents {

    private final String json;

    public JsonMessageContents(String json, MessageOutputs outputs) {
        super(outputs);
        this.json = json;
    }

    @Override
    public <T> T compose(ChatComposer<T> composer) {
        return composer.composeJson(this.json);
    }

}

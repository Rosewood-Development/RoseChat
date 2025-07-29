package dev.rosewood.rosechat.message.contents;

import dev.rosewood.rosechat.message.tokenizer.MessageOutputs;
import dev.rosewood.rosechat.message.tokenizer.composer.ChatComposer;

class LegacyMessageContents extends MessageContents {

    private final String legacyText;

    public LegacyMessageContents(String legacyText, MessageOutputs outputs) {
        super(outputs);
        this.legacyText = legacyText;
    }

    @Override
    public <T> T compose(ChatComposer<T> composer) {
        return composer.composeLegacy(this.legacyText);
    }

}

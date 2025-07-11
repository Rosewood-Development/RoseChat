package dev.rosewood.rosechat.message.contents;

import dev.rosewood.rosechat.message.tokenizer.MessageOutputs;
import dev.rosewood.rosechat.message.tokenizer.composer.ChatComposer;
import net.md_5.bungee.api.chat.BaseComponent;

class BungeeMessageContents extends MessageContents {

    private final BaseComponent[] components;

    public BungeeMessageContents(BaseComponent[] components, MessageOutputs outputs) {
        super(outputs);
        this.components = components;
    }

    @Override
    public <T> T compose(ChatComposer<T> composer) {
        return composer.composeBungee(this.components);
    }

}

package dev.rosewood.rosechat.message.contents;

import dev.rosewood.rosechat.message.tokenizer.MessageOutputs;
import dev.rosewood.rosechat.message.tokenizer.composer.ChatComposer;
import net.kyori.adventure.text.Component;

class AdventureMessageContents extends MessageContents {

    private final Component component;

    public AdventureMessageContents(Component component, MessageOutputs outputs) {
        super(outputs);
        this.component = component;
    }

    @Override
    public <T> T compose(ChatComposer<T> composer) {
        return composer.composeAdventure().compose(this.component);
    }

}

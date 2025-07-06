package dev.rosewood.rosechat.message.tokenizer.decorator.bungee;

import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.decorator.TokenDecorator;
import net.md_5.bungee.api.chat.BaseComponent;

public interface BungeeTokenDecorator extends TokenDecorator {

    /**
     * Applies this decorator to the given component.
     *
     * @param component The component to apply this decorator to
     * @param tokenizer The tokenizer
     * @param parent The parent token
     */
    void apply(BaseComponent component, MessageTokenizer tokenizer, Token parent);

}

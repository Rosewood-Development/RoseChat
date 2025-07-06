package dev.rosewood.rosechat.message.tokenizer.decorator.adventure;

import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.decorator.TokenDecorator;
import net.kyori.adventure.text.Component;

public interface AdventureTokenDecorator extends TokenDecorator {

    /**
     * Applies and returns a new component with the decorator applied.
     *
     * @param component The component to apply this decorator to
     * @param tokenizer The tokenizer
     * @param parent The parent token
     * @return A new component with the decorator applied
     */
    Component apply(Component component, MessageTokenizer tokenizer, Token parent);

}

package dev.rosewood.rosechat.message.tokenizer.composer;

import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import net.md_5.bungee.api.chat.BaseComponent;

public interface TokenComposer {

    /**
     * Composes the token from RoseChat's Token into Spigot's BaseComponents
     *
     * @param token The token to compose
     * @return The composed BaseComponent[]
     */
    BaseComponent[] compose(Token token);

    /**
     * @return a fully decorated token composer, includes all decorator types
     */
    static TokenComposer decorated(MessageTokenizer tokenizer) {
        return new FullyDecoratedTokenComposer(tokenizer);
    }

    /**
     * @return a token composer that only applies styles, ignores events such as hovers and clicks
     */
    static TokenComposer styles(MessageTokenizer tokenizer) {
        return new StylesOnlyTokenComposer(tokenizer);
    }

    /**
     * @return a token composer that applies no decoration, only raw text
     */
    static TokenComposer plain() {
        return new PlainTokenComposer();
    }

}

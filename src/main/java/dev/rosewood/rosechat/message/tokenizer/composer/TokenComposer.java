package dev.rosewood.rosechat.message.tokenizer.composer;

import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import net.md_5.bungee.api.chat.BaseComponent;

public interface TokenComposer<T> {

    /**
     * Composes the token from RoseChat's Token into another type
     *
     * @param token The token to compose
     * @return The composed content
     */
    T compose(Token token);

    /**
     * @return a fully decorated token composer, includes all decorator types
     */
    static TokenComposer<BaseComponent[]> decorated(MessageTokenizer tokenizer) {
        return new FullyDecoratedTokenComposer(tokenizer);
    }

    /**
     * @return a token composer that only applies styles, ignores events such as hovers and clicks
     */
    static TokenComposer<BaseComponent[]> styles(MessageTokenizer tokenizer) {
        return new StylesOnlyTokenComposer(tokenizer);
    }

    /**
     * @return a token composer that applies no decoration, only raw text
     */
    static TokenComposer<String> plain() {
        return new PlainTokenComposer();
    }

    /**
     * @return a token composer that only applies formatting using markdown
     */
    static TokenComposer<String> markdown() {
        return new MarkdownTokenComposer();
    }

}

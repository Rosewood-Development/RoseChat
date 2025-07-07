package dev.rosewood.rosechat.message.tokenizer.composer;

import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.composer.adventure.AdventureTokenComposers;
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
     * Composes and passes through raw text as this composed type.
     *
     * @param text The raw text to compose
     * @return The composed text
     * @throws UnsupportedOperationException if this method is not supported by the composer
     */
    T composeLegacyText(String text);

    /**
     * Composes and passes through raw json as this composed type.
     *
     * @param json The raw text to compose
     * @return The composed json
     * @throws UnsupportedOperationException if this method is not supported by the composer
     */
    T composeJson(String json);

    /**
     * @return a fully decorated token composer, includes all decorator types
     */
    static TokenComposer<BaseComponent[]> decorated() {
        return FullyDecoratedBungeeTokenComposer.INSTANCE;
    }

    /**
     * @return a token composer that only applies styles, ignores events such as hovers and clicks
     */
    static TokenComposer<BaseComponent[]> styles() {
        return StylesOnlyBungeeTokenComposer.INSTANCE;
    }

    /**
     * @return the separate Adventure token composers instance to avoid classloader issues when running on Spigot
     */
    static AdventureTokenComposers adventure() {
        return AdventureTokenComposers.INSTANCE;
    }

    /**
     * @return a token composer that applies no decoration, only raw text
     */
    static TokenComposer<String> plain() {
        return PlainTokenComposer.INSTANCE;
    }

    /**
     * @return a token composer that only applies formatting using markdown
     */
    static TokenComposer<String> markdown() {
        return MarkdownTokenComposer.INSTANCE;
    }

    /**
     * @return a token composer that outputs as a legacy formatted string
     */
    static TokenComposer<String> legacy() {
        return LegacyTextComposer.INSTANCE;
    }

    /**
     * @return a token composer that outputs as a json string
     */
    static TokenComposer<String> json() {
        return JsonComposer.INSTANCE;
    }

}

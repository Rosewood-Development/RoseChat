package dev.rosewood.rosechat.message.tokenizer.composer;

import dev.rosewood.rosechat.message.tokenizer.Token;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;

/**
 * Allows converting from one chat format to another
 */
public interface ChatComposer<T> {

    /**
     * Composes a token from RoseChat's format into another type
     *
     * @param token The token to compose
     * @return The composed type from a RoseChat token
     */
    T compose(Token token);

    /**
     * Composes a legacy Spigot text string into another type
     *
     * @param text The raw text to compose
     * @return The composed type from a legacy Spigot string
     * @throws UnsupportedOperationException if this method is not supported by the composer
     */
    T composeLegacy(String text);

    /**
     * Composes a json string into another type
     *
     * @param json The json string to compose
     * @return The composed type from json
     * @throws UnsupportedOperationException if this method is not supported by the composer
     */
    T composeJson(String json);

    /**
     * Composes a bungee components array into another type
     *
     * @param components The bungee components array to compose
     * @return The composed type from a bungee components array
     */
    T composeBungee(BaseComponent[] components);

    /**
     * @return the separate Adventure chat composer instance to avoid classloader issues when running on Spigot
     */
    Adventure<T> composeAdventure();

    /**
     * The separate Adventure chat composer instance to avoid classloader issues when running on Spigot
     */
    interface Adventure<T> {

        /**
         * Composes an Adventure component into another type
         *
         * @param component The Adventure component to compose
         * @return The composed type from an Adventure component
         */
        T compose(Component component);

    }

    /**
     * @return a fully decorated chat composer, includes all decorator types
     */
    static ChatComposer<BaseComponent[]> decorated() {
        return FullyDecoratedBungeeChatComposer.INSTANCE;
    }

    /**
     * @return a chat composer that only applies styles, ignores events such as hovers and clicks
     */
    static ChatComposer<BaseComponent[]> styles() {
        return StylesOnlyBungeeChatComposer.INSTANCE;
    }

    /**
     * @return a chat composer that applies no decoration, only raw text
     */
    static ChatComposer<String> plain() {
        return PlainChatComposer.INSTANCE;
    }

    /**
     * @return a chat composer that only applies formatting using markdown
     */
    static ChatComposer<String> markdown() {
        return MarkdownChatComposer.INSTANCE;
    }

    /**
     * @return a chat composer that outputs as a legacy formatted string
     */
    static ChatComposer<String> legacy() {
        return LegacyTextComposer.INSTANCE;
    }

    /**
     * @return a chat composer that outputs as a json string
     */
    static ChatComposer<String> json() {
        return JsonComposer.INSTANCE;
    }

    /**
     * @return the separate Adventure chat composer instance to avoid classloader issues when running on Spigot
     */
    static AdventureChatComposers adventure() {
        return AdventureChatComposers.INSTANCE;
    }

    final class AdventureChatComposers {

        public static final AdventureChatComposers INSTANCE = new AdventureChatComposers();

        private AdventureChatComposers() {

        }

        /**
         * @return a fully decorated token composer, includes all decorator types
         */
        public ChatComposer<Component> decorated() {
            return FullyDecoratedAdventureChatComposer.INSTANCE;
        }

        /**
         * @return a token composer that only applies styles, ignores events such as hovers and clicks
         */
        public ChatComposer<Component> styles() {
            return StylesOnlyAdventureChatComposer.INSTANCE;
        }

    }

}

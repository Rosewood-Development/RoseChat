package dev.rosewood.rosechat.message.tokenizer.composer.adventure;

import dev.rosewood.rosechat.message.tokenizer.composer.TokenComposer;
import net.kyori.adventure.text.Component;

public final class AdventureTokenComposers {

    public static final AdventureTokenComposers INSTANCE = new AdventureTokenComposers();

    private AdventureTokenComposers() {

    }

    /**
     * @return a fully decorated token composer, includes all decorator types
     */
    public TokenComposer<Component> decorated() {
        return FullyDecoratedAdventureTokenComposer.INSTANCE;
    }

    /**
     * @return a token composer that only applies styles, ignores events such as hovers and clicks
     */
    public TokenComposer<Component> styles() {
        return StylesOnlyAdventureTokenComposer.INSTANCE;
    }

}

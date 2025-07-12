package dev.rosewood.rosechat.message.tokenizer.composer;

import dev.rosewood.rosechat.message.tokenizer.composer.decorator.adventure.AdventureTokenDecorators;
import dev.rosewood.rosechat.message.tokenizer.decorator.DecoratorType;
import dev.rosewood.rosechat.message.tokenizer.decorator.TokenDecorator;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;

public class StylesOnlyAdventureChatComposer extends FullyDecoratedAdventureChatComposer {

    public static final StylesOnlyAdventureChatComposer INSTANCE = new StylesOnlyAdventureChatComposer();

    private StylesOnlyAdventureChatComposer() {

    }

    @Override
    protected AdventureTokenDecorators createDecorators() {
        return new StyledTokenDecorators();
    }

    @Override
    protected AdventureTokenDecorators createDecorators(AdventureTokenDecorators contextDecorators) {
        return new StyledTokenDecorators(contextDecorators);
    }

    @Override
    public Component composeLegacy(String text) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Component composeJson(String json) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Not implemented.
     * @throws UnsupportedOperationException always
     */
    @Override
    public Component composeBungee(BaseComponent[] components) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ChatComposer.Adventure<Component> composeAdventure() {
        return Adventure.INSTANCE;
    }

    public static final class Adventure implements ChatComposer.Adventure<Component> {

        private static final Adventure INSTANCE = new Adventure();

        private Adventure() {

        }

        /**
         * Not implemented.
         * @throws UnsupportedOperationException always
         */
        @Override
        public Component compose(Component component) {
            throw new UnsupportedOperationException("Not implemented");
        }

    }

    private static class StyledTokenDecorators extends AdventureTokenDecorators {

        public StyledTokenDecorators() {
            super();
        }

        public StyledTokenDecorators(AdventureTokenDecorators decorators) {
            super(decorators);
        }

        @Override
        public void add(List<? extends TokenDecorator> toAdd) {
            List<? extends TokenDecorator> filtered = toAdd.stream().filter(x -> x.getType() == DecoratorType.STYLING).toList();
            super.add(filtered);
        }

    }

}

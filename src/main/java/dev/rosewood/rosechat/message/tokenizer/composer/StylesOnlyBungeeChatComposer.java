package dev.rosewood.rosechat.message.tokenizer.composer;

import dev.rosewood.rosechat.message.tokenizer.composer.decorator.bungee.BungeeTokenDecorators;
import dev.rosewood.rosechat.message.tokenizer.decorator.DecoratorType;
import dev.rosewood.rosechat.message.tokenizer.decorator.TokenDecorator;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;

public class StylesOnlyBungeeChatComposer extends FullyDecoratedBungeeChatComposer {

    public static final StylesOnlyBungeeChatComposer INSTANCE = new StylesOnlyBungeeChatComposer();

    private StylesOnlyBungeeChatComposer() {

    }

    @Override
    protected BungeeTokenDecorators createDecorators() {
        return new StyledTokenDecorators();
    }

    @Override
    protected BungeeTokenDecorators createDecorators(BungeeTokenDecorators contextDecorators) {
        return new StyledTokenDecorators(contextDecorators);
    }

    @Override
    public BaseComponent[] composeLegacy(String text) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public BaseComponent[] composeJson(String json) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Not implemented.
     * @throws UnsupportedOperationException always
     */
    @Override
    public BaseComponent[] composeBungee(BaseComponent[] components) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ChatComposer.Adventure<BaseComponent[]> composeAdventure() {
        return Adventure.INSTANCE;
    }

    public static final class Adventure implements ChatComposer.Adventure<BaseComponent[]> {

        private static final Adventure INSTANCE = new Adventure();

        private Adventure() {

        }

        /**
         * Not implemented.
         * @throws UnsupportedOperationException always
         */
        @Override
        public BaseComponent[] compose(Component component) {
            throw new UnsupportedOperationException("Not implemented");
        }

    }

    private static class StyledTokenDecorators extends BungeeTokenDecorators {

        public StyledTokenDecorators() {
            super();
        }

        public StyledTokenDecorators(BungeeTokenDecorators decorators) {
            super(decorators);
        }

        @Override
        public void add(List<? extends TokenDecorator> toAdd) {
            List<? extends TokenDecorator> filtered = toAdd.stream().filter(x -> x.getType() == DecoratorType.STYLING).toList();
            super.add(filtered);
        }

    }

}

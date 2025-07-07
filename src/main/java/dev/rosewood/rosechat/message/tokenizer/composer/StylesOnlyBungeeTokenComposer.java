package dev.rosewood.rosechat.message.tokenizer.composer;

import dev.rosewood.rosechat.message.tokenizer.composer.decorator.bungee.BungeeTokenDecorators;
import dev.rosewood.rosechat.message.tokenizer.decorator.DecoratorType;
import dev.rosewood.rosechat.message.tokenizer.decorator.TokenDecorator;
import java.util.List;
import net.md_5.bungee.api.chat.BaseComponent;

public class StylesOnlyBungeeTokenComposer extends FullyDecoratedBungeeTokenComposer {

    public static final StylesOnlyBungeeTokenComposer INSTANCE = new StylesOnlyBungeeTokenComposer();

    private StylesOnlyBungeeTokenComposer() {

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
    public BaseComponent[] composeLegacyText(String text) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public BaseComponent[] composeJson(String json) {
        throw new UnsupportedOperationException("Not implemented");
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

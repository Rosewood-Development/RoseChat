package dev.rosewood.rosechat.message.tokenizer.composer.adventure;

import dev.rosewood.rosechat.message.tokenizer.composer.decorator.adventure.AdventureTokenDecorators;
import dev.rosewood.rosechat.message.tokenizer.decorator.DecoratorType;
import dev.rosewood.rosechat.message.tokenizer.decorator.TokenDecorator;
import java.util.List;
import net.kyori.adventure.text.Component;

public class StylesOnlyAdventureTokenComposer extends FullyDecoratedAdventureTokenComposer {

    public static final StylesOnlyAdventureTokenComposer INSTANCE = new StylesOnlyAdventureTokenComposer();

    private StylesOnlyAdventureTokenComposer() {

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
    public Component composeLegacyText(String text) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Component composeJson(String json) {
        throw new UnsupportedOperationException("Not implemented");
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

package dev.rosewood.rosechat.message.tokenizer.composer.adventure;

import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.tokenizer.composer.decorator.AdventureTokenDecorators;
import dev.rosewood.rosechat.message.tokenizer.decorator.DecoratorType;
import dev.rosewood.rosechat.message.tokenizer.decorator.TokenDecorator;
import java.util.List;

public class StylesOnlyAdventureTokenComposer extends FullyDecoratedAdventureTokenComposer {

    protected StylesOnlyAdventureTokenComposer(MessageTokenizer tokenizer) {
        super(tokenizer);
    }

    @Override
    protected AdventureTokenDecorators createDecorators() {
        return new StyledTokenDecorators();
    }

    @Override
    protected AdventureTokenDecorators createDecorators(AdventureTokenDecorators contextDecorators) {
        return new StyledTokenDecorators(contextDecorators);
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

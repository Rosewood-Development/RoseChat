package dev.rosewood.rosechat.message.tokenizer.composer;

import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.tokenizer.composer.decorator.BungeeTokenDecorators;
import dev.rosewood.rosechat.message.tokenizer.decorator.DecoratorType;
import dev.rosewood.rosechat.message.tokenizer.decorator.TokenDecorator;
import java.util.List;

public class StylesOnlyBungeeTokenComposer extends FullyDecoratedBungeeTokenComposer {

    protected StylesOnlyBungeeTokenComposer(MessageTokenizer tokenizer) {
        super(tokenizer);
    }

    @Override
    protected BungeeTokenDecorators createDecorators() {
        return new StyledTokenDecorators();
    }

    @Override
    protected BungeeTokenDecorators createDecorators(BungeeTokenDecorators contextDecorators) {
        return new StyledTokenDecorators(contextDecorators);
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

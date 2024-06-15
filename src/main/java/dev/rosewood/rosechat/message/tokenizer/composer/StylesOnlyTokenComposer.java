package dev.rosewood.rosechat.message.tokenizer.composer;

import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.tokenizer.decorator.DecoratorType;
import dev.rosewood.rosechat.message.tokenizer.decorator.TokenDecorator;
import dev.rosewood.rosechat.message.tokenizer.decorator.TokenDecorators;
import java.util.List;

public class StylesOnlyTokenComposer extends FullyDecoratedTokenComposer {

    protected StylesOnlyTokenComposer(MessageTokenizer tokenizer) {
        super(tokenizer);
    }

    @Override
    protected TokenDecorators createDecorators() {
        return new StyledTokenDecorators();
    }

    @Override
    protected TokenDecorators createDecorators(TokenDecorators contextDecorators) {
        return new StyledTokenDecorators(contextDecorators);
    }

    private static class StyledTokenDecorators extends TokenDecorators {

        public StyledTokenDecorators() {
            super();
        }

        public StyledTokenDecorators(TokenDecorators decorators) {
            super(decorators);
        }

        @Override
        public void add(List<TokenDecorator> toAdd) {
            List<TokenDecorator> filtered = toAdd.stream().filter(x -> x.getType() == DecoratorType.STYLING).toList();
            super.add(filtered);
        }

    }

}

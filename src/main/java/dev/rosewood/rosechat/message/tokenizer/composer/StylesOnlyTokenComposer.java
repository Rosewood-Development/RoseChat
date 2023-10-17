package dev.rosewood.rosechat.message.tokenizer.composer;

import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.decorator.DecoratorType;
import dev.rosewood.rosechat.message.tokenizer.decorator.TokenDecorator;
import dev.rosewood.rosechat.message.tokenizer.decorator.TokenDecorators;
import java.util.List;
import net.md_5.bungee.api.chat.BaseComponent;

public class StylesOnlyTokenComposer extends FullyDecoratedTokenComposer {

    protected StylesOnlyTokenComposer(MessageTokenizer tokenizer) {
        super(tokenizer);
    }

    @Override
    public BaseComponent[] compose(Token token) {
        return this.compose(token, new StyledTokenDecorators());
    }

    private static class StyledTokenDecorators extends TokenDecorators {

        @Override
        public void add(List<TokenDecorator> toAdd) {
            List<TokenDecorator> filtered = toAdd.stream().filter(x -> x.getType() == DecoratorType.STYLING).toList();
            super.add(filtered);
        }

    }

}

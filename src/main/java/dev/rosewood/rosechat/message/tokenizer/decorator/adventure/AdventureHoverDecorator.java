package dev.rosewood.rosechat.message.tokenizer.decorator.adventure;

import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.composer.TokenComposer;
import dev.rosewood.rosechat.message.tokenizer.decorator.HoverDecorator;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.inventory.ItemStack;

public class AdventureHoverDecorator extends HoverDecorator implements AdventureTokenDecorator {

    public AdventureHoverDecorator(List<String> content) {
        super(content);
    }

    public AdventureHoverDecorator(String content) {
        super(List.of(content));
    }

    public AdventureHoverDecorator(ItemStack itemStack, String nbt) {
        super(itemStack, nbt);
    }

    @Override
    public Component apply(Component component, MessageTokenizer tokenizer, Token parent) {
        return switch (this.action) {
            case SHOW_TEXT -> {
                if (this.content == null || this.content.isEmpty())
                    yield component;

                // Append all lines together separated by \n and run it all through the tokenizer so colors pass through newlines
                // I still can't believe this works
                String combinedContent = String.join("\n", this.content);

                Token.Builder builder = Token.group(combinedContent).placeholders(parent.getPlaceholders());
                parent.getIgnoredTokenizers().forEach(builder::ignoreTokenizer);

                Token token = builder.build();
                tokenizer.tokenize(token, tokenizer.getLastDecoratorFactory());
                Component hover = tokenizer.compose(token, TokenComposer.adventure().styles(tokenizer));
                yield component.hoverEvent(HoverEvent.showText(hover));
            }
            case SHOW_ITEM -> component.hoverEvent(this.item.asHoverEvent());
        };
    }

}

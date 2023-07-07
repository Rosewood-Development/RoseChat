package dev.rosewood.rosechat.message.tokenizer.decorator;

import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;

public class ColorDecorator extends TokenDecorator {

    private final ChatColor chatColor;

    private ColorDecorator(ChatColor chatColor) {
        super(DecoratorType.STYLING);
        this.chatColor = chatColor;
    }

    @Override
    public void apply(BaseComponent component, MessageTokenizer tokenizer, StringPlaceholders placeholders) {
        component.setColor(this.chatColor);
    }

    @Override
    public boolean isOverwrittenBy(TokenDecorator newDecorator) {
        if (newDecorator instanceof FormatDecorator formatDecorator)
            return formatDecorator.chatColor == ChatColor.RESET;
        return super.isOverwrittenBy(newDecorator);
    }

    public static ColorDecorator of(ChatColor chatColor) {
        return new ColorDecorator(chatColor);
    }

}

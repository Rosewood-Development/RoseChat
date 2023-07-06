package dev.rosewood.rosechat.message.tokenizer.decorator;

import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;

public class ColorDecorator extends TokenDecorator {

    private final ChatColor chatColor;

    private ColorDecorator(ChatColor chatColor) {
        super(DecoratorType.STYLING);
        this.chatColor = chatColor;
    }

    @Override
    public void apply(BaseComponent component, MessageTokenizer tokenizer) {
        component.setColor(this.chatColor);
    }

    @Override
    public boolean isOverwrittenBy(TokenDecorator newDecorator) {
        if (newDecorator instanceof FormatDecorator formatDecorator)
            return formatDecorator.chatColor == ChatColor.RESET;
        return newDecorator instanceof ColorDecorator;
    }

    public static ColorDecorator of(ChatColor chatColor) {
        return new ColorDecorator(chatColor);
    }

}

package dev.rosewood.rosechat.message.tokenizer.decorator;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;

public class ColorDecorator implements TokenDecorator {

    private final ChatColor chatColor;

    private ColorDecorator(ChatColor chatColor) {
        this.chatColor = chatColor;
    }

    @Override
    public void apply(BaseComponent component) {
        component.setColor(this.chatColor);
    }

    @Override
    public boolean isOverwrittenBy(TokenDecorator newDecorator) {
        return newDecorator instanceof ColorDecorator;
    }

    public static ColorDecorator of(ChatColor chatColor) {
        return new ColorDecorator(chatColor);
    }

}

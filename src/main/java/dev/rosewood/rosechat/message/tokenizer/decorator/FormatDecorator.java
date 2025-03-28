package dev.rosewood.rosechat.message.tokenizer.decorator;

import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;

public class FormatDecorator extends TokenDecorator {

    protected final ChatColor chatColor;
    private final boolean value;

    private FormatDecorator(ChatColor chatColor, boolean value) {
        super(DecoratorType.STYLING);
        this.chatColor = chatColor;
        this.value = value;
    }

    @Override
    public void apply(BaseComponent component, MessageTokenizer tokenizer, Token parent) {
        if (this.chatColor == ChatColor.BOLD) {
            if (this.value)
                component.setBold(true);
        } else if (this.chatColor == ChatColor.ITALIC) {
            if (this.value)
                component.setItalic(true);
        } else if (this.chatColor == ChatColor.UNDERLINE) {
            if (this.value)
                component.setUnderlined(true);
        } else if (this.chatColor == ChatColor.STRIKETHROUGH) {
            if (this.value)
                component.setStrikethrough(true);
        } else if (this.chatColor == ChatColor.MAGIC) {
            if (this.value)
                component.setObfuscated(true);
        }
    }

    @Override
    public boolean isOverwrittenBy(TokenDecorator newDecorator) {
        if (newDecorator instanceof FormatDecorator otherFormat) {
            if (otherFormat.chatColor == ChatColor.RESET)
                return true;

            if (otherFormat.chatColor == this.chatColor) {
                return !otherFormat.value && this.value;
            } else {
                return false;
            }
        }
        return newDecorator instanceof ColorDecorator;
    }

    @Override
    public boolean isMarker() {
        return !this.value;
    }

    public ChatColor getChatColor() {
        return this.chatColor;
    }

    public static FormatDecorator of(ChatColor chatColor, boolean value) {
        return new FormatDecorator(chatColor, value);
    }

}

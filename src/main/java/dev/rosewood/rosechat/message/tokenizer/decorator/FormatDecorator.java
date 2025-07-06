package dev.rosewood.rosechat.message.tokenizer.decorator;

import net.md_5.bungee.api.ChatColor;

public abstract class FormatDecorator implements TokenDecorator {

    protected FormatType formatType;
    protected boolean value;

    protected FormatDecorator(FormatType formatType, boolean value) {
        this.formatType = formatType;
        this.value = value;
    }

    protected FormatDecorator(ChatColor chatColor, boolean value) {
        this.formatType = chatColorToFormatType(chatColor);
        this.value = value;
    }

    @Override
    public DecoratorType getType() {
        return DecoratorType.STYLING;
    }

    @Override
    public boolean isOverwrittenBy(TokenDecorator newDecorator) {
        if (newDecorator instanceof FormatDecorator otherFormat) {
            if (otherFormat.formatType == FormatType.RESET)
                return true;

            if (otherFormat.formatType == this.formatType) {
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

    public FormatType getFormatType() {
        return this.formatType;
    }

    protected static FormatType chatColorToFormatType(ChatColor chatColor) {
        return FormatType.values()[chatColor.ordinal() - ChatColor.MAGIC.ordinal()]; // I feel like I'm committing crimes
    }

    public enum FormatType {
        MAGIC,
        BOLD,
        STRIKETHROUGH,
        UNDERLINE,
        ITALIC,
        RESET
    }

}

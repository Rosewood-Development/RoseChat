package dev.rosewood.rosechat.message.tokenizer.decorator;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;

public class FormatDecorator implements TokenDecorator {

    private final ChatColor chatColor;
    private final boolean value;

    private FormatDecorator(ChatColor chatColor, boolean value) {
        this.chatColor = chatColor;
        this.value = value;
    }

    @Override
    public void apply(BaseComponent component) {
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
        } else if (this.chatColor == ChatColor.RESET) {
            component.setColor(ChatColor.WHITE); // TODO: Reset to player chat color
        }
    }

    @Override
    public boolean isOverwrittenBy(TokenDecorator newDecorator) {
        if (!(newDecorator instanceof FormatDecorator))
            return false;

        FormatDecorator other = (FormatDecorator) newDecorator;
        if (other.chatColor == ChatColor.RESET)
            return true;

        return other.chatColor == this.chatColor;
    }

    @Override
    public boolean isMarker() {
        return !this.value;
    }

    public static FormatDecorator of(ChatColor chatColor, boolean value) {
        return new FormatDecorator(chatColor, value);
    }

}

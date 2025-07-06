package dev.rosewood.rosechat.message.tokenizer.decorator.bungee;

import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.decorator.FormatDecorator;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;

public class BungeeFormatDecorator extends FormatDecorator implements BungeeTokenDecorator {

    public BungeeFormatDecorator(FormatType formatType, boolean value) {
        super(formatType, value);
    }

    public BungeeFormatDecorator(ChatColor chatColor, boolean value) {
        super(chatColor, value);
    }

    @Override
    public void apply(BaseComponent component, MessageTokenizer tokenizer, Token parent) {
        if (this.formatType == FormatType.BOLD) {
            if (this.value)
                component.setBold(true);
        } else if (this.formatType == FormatType.ITALIC) {
            if (this.value)
                component.setItalic(true);
        } else if (this.formatType == FormatType.UNDERLINE) {
            if (this.value)
                component.setUnderlined(true);
        } else if (this.formatType == FormatType.STRIKETHROUGH) {
            if (this.value)
                component.setStrikethrough(true);
        } else if (this.formatType == FormatType.MAGIC) {
            if (this.value)
                component.setObfuscated(true);
        }
    }

}

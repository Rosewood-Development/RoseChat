package dev.rosewood.rosechat.message.tokenizer.decorator.adventure;

import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.decorator.FormatDecorator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.ChatColor;

public class AdventureFormatDecorator extends FormatDecorator implements AdventureTokenDecorator {

    public AdventureFormatDecorator(FormatType formatType, boolean value) {
        super(formatType, value);
    }

    public AdventureFormatDecorator(ChatColor chatColor, boolean value) {
        super(chatColor, value);
    }

    @Override
    public Component apply(Component component, MessageTokenizer tokenizer, Token parent) {
        if (this.formatType == FormatType.BOLD) {
            if (this.value)
                return component.decorate(TextDecoration.BOLD);
        } else if (this.formatType == FormatType.ITALIC) {
            if (this.value)
                return component.decorate(TextDecoration.ITALIC);
        } else if (this.formatType == FormatType.UNDERLINE) {
            if (this.value)
                return component.decorate(TextDecoration.UNDERLINED);
        } else if (this.formatType == FormatType.STRIKETHROUGH) {
            if (this.value)
                return component.decorate(TextDecoration.STRIKETHROUGH);
        } else if (this.formatType == FormatType.MAGIC) {
            if (this.value)
                return component.decorate(TextDecoration.OBFUSCATED);
        }
        return component;
    }

}

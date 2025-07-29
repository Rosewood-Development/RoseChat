package dev.rosewood.rosechat.message.tokenizer.composer.decorator.bungee;

import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.decorator.FormatDecorator;
import net.md_5.bungee.api.chat.BaseComponent;

public class BungeeFormatDecorator extends BungeeTokenDecorator<FormatDecorator> {

    public BungeeFormatDecorator(FormatDecorator decorator) {
        super(decorator);
    }

    @Override
    public void apply(BaseComponent component, Token parent) {
        FormatDecorator.FormatType formatType = this.decorator.formatType();
        boolean value = this.decorator.value();
        if (formatType == FormatDecorator.FormatType.BOLD) {
            if (value)
                component.setBold(true);
        } else if (formatType == FormatDecorator.FormatType.ITALIC) {
            if (value)
                component.setItalic(true);
        } else if (formatType == FormatDecorator.FormatType.UNDERLINE) {
            if (value)
                component.setUnderlined(true);
        } else if (formatType == FormatDecorator.FormatType.STRIKETHROUGH) {
            if (value)
                component.setStrikethrough(true);
        } else if (formatType == FormatDecorator.FormatType.MAGIC) {
            if (value)
                component.setObfuscated(true);
        }
    }

}

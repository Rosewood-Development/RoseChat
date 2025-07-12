package dev.rosewood.rosechat.message.tokenizer.composer.decorator.adventure;

import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.decorator.FormatDecorator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

public class AdventureFormatDecorator extends AdventureTokenDecorator<FormatDecorator> {

    public AdventureFormatDecorator(FormatDecorator decorator) {
        super(decorator);
    }

    @Override
    public Component apply(Component component, Token parent) {
        FormatDecorator.FormatType formatType = this.decorator.formatType();
        boolean value = this.decorator.value();
        if (formatType == FormatDecorator.FormatType.BOLD) {
            if (value)
                return component.decorate(TextDecoration.BOLD);
        } else if (formatType == FormatDecorator.FormatType.ITALIC) {
            if (value)
                return component.decorate(TextDecoration.ITALIC);
        } else if (formatType == FormatDecorator.FormatType.UNDERLINE) {
            if (value)
                return component.decorate(TextDecoration.UNDERLINED);
        } else if (formatType == FormatDecorator.FormatType.STRIKETHROUGH) {
            if (value)
                return component.decorate(TextDecoration.STRIKETHROUGH);
        } else if (formatType == FormatDecorator.FormatType.MAGIC) {
            if (value)
                return component.decorate(TextDecoration.OBFUSCATED);
        }
        return component;
    }

}

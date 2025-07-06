package dev.rosewood.rosechat.message.tokenizer.decorator.adventure;

import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.decorator.ColorDecorator;
import dev.rosewood.rosegarden.utils.HexUtils;
import java.util.function.Function;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.md_5.bungee.api.ChatColor;

public class AdventureColorDecorator extends ColorDecorator implements AdventureTokenDecorator {

    public AdventureColorDecorator(Function<Integer, HexUtils.ColorGenerator> colorGeneratorFunction) {
        super(colorGeneratorFunction);
    }

    public AdventureColorDecorator(ChatColor chatColor) {
        super(chatColor);
    }

    @Override
    public Component apply(Component component, MessageTokenizer tokenizer, Token parent) {
        if (this.colorGenerator == null && this.colorGeneratorFunction != null)
            this.colorGenerator = this.colorGeneratorFunction.apply(tokenizer.findDecoratorContentLength(parent, this));

        if (this.colorGenerator != null && (!(component instanceof TextComponent textComponent) || !textComponent.content().isBlank()))
            return component.color(TextColor.color(this.colorGenerator.nextChatColor().getColor().getRGB()));

        return component;
    }

}

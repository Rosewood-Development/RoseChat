package dev.rosewood.rosechat.message.tokenizer.decorator.bungee;

import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.decorator.ColorDecorator;
import dev.rosewood.rosegarden.utils.HexUtils;
import java.util.function.Function;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class BungeeColorDecorator extends ColorDecorator implements BungeeTokenDecorator {

    public BungeeColorDecorator(Function<Integer, HexUtils.ColorGenerator> colorGeneratorFunction) {
        super(colorGeneratorFunction);
    }

    public BungeeColorDecorator(ChatColor chatColor) {
        super(chatColor);
    }

    @Override
    public void apply(BaseComponent component, MessageTokenizer tokenizer, Token parent) {
        if (this.colorGenerator == null && this.colorGeneratorFunction != null)
            this.colorGenerator = this.colorGeneratorFunction.apply(tokenizer.findDecoratorContentLength(parent, this));

        if (this.colorGenerator != null && (!(component instanceof TextComponent textComponent) || !textComponent.getText().isBlank()))
            component.setColor(this.colorGenerator.nextChatColor());
    }

}

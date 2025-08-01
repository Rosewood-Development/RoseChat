package dev.rosewood.rosechat.message.tokenizer.composer.decorator.bungee;

import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.decorator.ShadowColorDecorator;
import dev.rosewood.rosegarden.utils.HexUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class BungeeShadowColorDecorator extends BungeeTokenDecorator<ShadowColorDecorator> {

    private HexUtils.ColorGenerator colorGenerator;

    public BungeeShadowColorDecorator(ShadowColorDecorator decorator) {
        super(decorator);
    }

    @Override
    public void apply(BaseComponent component, Token parent) {
        if (this.colorGenerator == null && this.decorator.colorGeneratorFunction() != null) {
            int contentLength = 0;
            if (this.decorator.blocksTextStitching())
                contentLength = MessageTokenizer.findDecoratorContentLength(parent, this.decorator);
            this.colorGenerator = this.decorator.colorGeneratorFunction().apply(contentLength);
        }

        if (!this.decorator.isMarker() && this.colorGenerator != null && (!(component instanceof TextComponent textComponent) || !textComponent.getText().isBlank()))
            component.setShadowColor(this.colorGenerator.nextChatColor().getColor());
    }

}

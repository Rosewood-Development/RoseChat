package dev.rosewood.rosechat.message.wrapper.tokenizer.rainbow;

import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class RainbowToken extends Token {

    public RainbowToken(RoseSender sender, RoseSender viewer, String originalContent) {
        super(sender, viewer, originalContent);
    }

    @Override
    public BaseComponent[] toComponents() {
        ComponentBuilder componentBuilder = new ComponentBuilder();
        for (char c : this.getOriginalContent().toCharArray()) {
            componentBuilder.append(String.valueOf(c));
        }

        return componentBuilder.create();
    }
}

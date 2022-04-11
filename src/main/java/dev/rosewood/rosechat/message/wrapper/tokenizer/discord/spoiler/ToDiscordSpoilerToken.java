package dev.rosewood.rosechat.message.wrapper.tokenizer.discord.spoiler;

import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class ToDiscordSpoilerToken extends Token {

    private final String replacement;

    public ToDiscordSpoilerToken(RoseSender sender, RoseSender viewer, String originalContent, String replacement) {
        super(sender, viewer, originalContent);
        this.replacement = replacement;
    }

    @Override
    public BaseComponent[] toComponents() {
        ComponentBuilder componentBuilder = new ComponentBuilder();
        componentBuilder.append("|").append("|");

        for (char c : this.replacement.toCharArray()) {
            componentBuilder.append(String.valueOf(c), ComponentBuilder.FormatRetention.NONE);
        }

        componentBuilder.append("|").append("|");
        return componentBuilder.create();
    }
}

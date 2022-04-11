package dev.rosewood.rosechat.message.wrapper.tokenizer.discord.tag;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class ToDiscordTagToken extends Token {

    public ToDiscordTagToken(RoseSender sender, RoseSender viewer, String originalContent) {
        super(sender, viewer, originalContent);
    }

    @Override
    public BaseComponent[] toComponents() {
        String name = this.getOriginalContent().substring(1);
        String discordTag = RoseChatAPI.getInstance().getDiscord().getUserTag(name);

        ComponentBuilder componentBuilder = new ComponentBuilder();
        for (char c : discordTag.toCharArray()) {
            componentBuilder.append(String.valueOf(c));
        }

        return componentBuilder.create();
    }
}

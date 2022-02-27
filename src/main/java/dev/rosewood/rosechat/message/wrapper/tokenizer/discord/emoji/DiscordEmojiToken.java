package dev.rosewood.rosechat.message.wrapper.tokenizer.discord.emoji;

import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class DiscordEmojiToken extends Token {

    private final String emoji;

    public DiscordEmojiToken(RoseSender sender, RoseSender viewer, String originalContent, String emoji) {
        super(sender, viewer, originalContent);
        this.emoji = emoji;
    }

    @Override
    public BaseComponent[] toComponents() {
        return TextComponent.fromLegacyText(this.asString());
    }

    @Override
    public String asString() {
        return ":" + this.emoji + ":";
    }
}

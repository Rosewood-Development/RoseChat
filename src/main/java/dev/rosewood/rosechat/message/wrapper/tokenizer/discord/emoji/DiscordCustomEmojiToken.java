package dev.rosewood.rosechat.message.wrapper.tokenizer.discord.emoji;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class DiscordCustomEmojiToken extends Token {

    private final String emoji;

    public DiscordCustomEmojiToken(RoseSender sender, RoseSender viewer, String originalContent, String emoji) {
        super(sender, viewer, originalContent);
        this.emoji = emoji;
    }

    @Override
    public BaseComponent[] toComponents() {
        ComponentBuilder componentBuilder = new ComponentBuilder();

        String content = RoseChatAPI.getInstance().getDiscord().getCustomEmoji(this.emoji);
        for (char c : (content == null ? ":" + this.emoji + ":" : content).toCharArray()) {
            componentBuilder.append(String.valueOf(c), ComponentBuilder.FormatRetention.NONE);
        }

        return componentBuilder.create();
    }
}

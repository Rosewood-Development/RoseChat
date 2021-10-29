package dev.rosewood.rosechat.message.wrapper.tokenizer.discord.from;

import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;

public class FromDiscordToken extends Token {

    public FromDiscordToken(RoseSender sender, RoseSender viewer, String originalContent) {
        super(sender, viewer, originalContent);
    }
}

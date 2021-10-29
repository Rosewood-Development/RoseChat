package dev.rosewood.rosechat.message.wrapper.tokenizer.discord.to;

import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;

public class ToDiscordToken extends Token {

    public ToDiscordToken(RoseSender sender, RoseSender viewer, String originalContent) {
        super(sender, viewer, originalContent);
    }
}

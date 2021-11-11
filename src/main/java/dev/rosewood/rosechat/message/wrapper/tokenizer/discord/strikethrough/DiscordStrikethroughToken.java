package dev.rosewood.rosechat.message.wrapper.tokenizer.discord.strikethrough;

import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;

public class DiscordStrikethroughToken extends Token {

    public DiscordStrikethroughToken(RoseSender sender, RoseSender viewer, String originalContent) {
        super(sender, viewer, originalContent);
    }

    @Override
    public String asString() {
        String content = this.getOriginalContent().substring(2, this.getOriginalContent().length() - 2);
        return Setting.DISCORD_FORMAT_STRIKETHROUGH.getString() + content + "&r";
    }
}

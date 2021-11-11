package dev.rosewood.rosechat.message.wrapper.tokenizer.discord.code;

import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;

public class DiscordMultiCodeToken extends Token {

    public DiscordMultiCodeToken(RoseSender sender, RoseSender viewer, String originalContent) {
        super(sender, viewer, originalContent);
    }

    @Override
    public String asString() {
        String content = this.getOriginalContent().substring(3, this.getOriginalContent().length() - 3).trim();
        return Setting.DISCORD_FORMAT_CODE_BLOCK_MULTIPLE.getString() + content + "&r";
    }
}

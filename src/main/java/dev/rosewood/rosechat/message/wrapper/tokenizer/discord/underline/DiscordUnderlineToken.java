package dev.rosewood.rosechat.message.wrapper.tokenizer.discord.underline;

import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;

public class DiscordUnderlineToken extends Token {

    public DiscordUnderlineToken(RoseSender sender, RoseSender viewer, String originalContent) {
        super(sender, viewer, originalContent);
    }

    @Override
    public String asString() {
        String content = this.getOriginalContent().substring(2, this.getOriginalContent().length() - 2);
        return Setting.DISCORD_FORMAT_UNDERLINE.getString().contains("%message%") ?
                Setting.DISCORD_FORMAT_UNDERLINE.getString().replace("%message%", content) :
                Setting.DISCORD_FORMAT_UNDERLINE.getString() + content + "&r";
    }
}

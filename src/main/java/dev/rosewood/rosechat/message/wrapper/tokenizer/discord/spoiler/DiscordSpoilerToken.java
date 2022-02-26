package dev.rosewood.rosechat.message.wrapper.tokenizer.discord.spoiler;

import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class DiscordSpoilerToken extends Token {

    public DiscordSpoilerToken(RoseSender sender, RoseSender viewer, String originalContent) {
        super(sender, viewer, originalContent);
    }

    @Override
    public BaseComponent[] toComponents() {
        return TextComponent.fromLegacyText(this.asString());
    }

    @Override
    public String asString() {
        String content = this.getOriginalContent().substring(2, this.getOriginalContent().length() - 2);
        return Setting.DISCORD_FORMAT_SPOILER.getString().contains("%message%") ?
                Setting.DISCORD_FORMAT_SPOILER.getString().replace("%message%", content) :
                Setting.DISCORD_FORMAT_SPOILER.getString() + content + "&r";
    }
}

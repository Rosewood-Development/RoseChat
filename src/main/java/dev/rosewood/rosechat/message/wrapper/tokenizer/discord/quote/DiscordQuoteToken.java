package dev.rosewood.rosechat.message.wrapper.tokenizer.discord.quote;

import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class DiscordQuoteToken extends Token {

    public DiscordQuoteToken(RoseSender sender, RoseSender viewer, String originalContent) {
        super(sender, viewer, originalContent);
    }

    @Override
    public BaseComponent[] toComponents() {
        return TextComponent.fromLegacyText(this.asString());
    }

    @Override
    public String asString() {
        String content = this.getOriginalContent().substring(2);
        return Setting.DISCORD_FORMAT_BLOCK_QUOTES.getString().contains("%message%") ?
                Setting.DISCORD_FORMAT_BLOCK_QUOTES.getString().replace("%message%", content) :
                Setting.DISCORD_FORMAT_BLOCK_QUOTES.getString() + content + "&r";
    }
}

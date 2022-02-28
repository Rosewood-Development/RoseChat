package dev.rosewood.rosechat.message.wrapper.tokenizer.discord.italic;

import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;

public class DiscordItalicToken extends Token {

    public DiscordItalicToken(RoseSender sender, RoseSender viewer, String originalContent) {
        super(sender, viewer, originalContent);
    }

    @Override
    public BaseComponent[] toComponents() {
        ComponentBuilder componentBuilder = new ComponentBuilder();
        for (char c : this.asString().toCharArray()) {
            componentBuilder.append(String.valueOf(c));
        }

        return componentBuilder.create();
    }

    @Override
    public String asString() {
        String content = this.getOriginalContent().substring(1, this.getOriginalContent().length() - 1);
        return Setting.DISCORD_FORMAT_ITALIC.getString().contains("%message%") ?
                Setting.DISCORD_FORMAT_ITALIC.getString().replace("%message%", content) :
                Setting.DISCORD_FORMAT_ITALIC.getString() + content + "&r";
    }
}

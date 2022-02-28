package dev.rosewood.rosechat.message.wrapper.tokenizer.discord.bold;

import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;

public class DiscordBoldToken extends Token {

    public DiscordBoldToken(RoseSender sender, RoseSender viewer, String originalContent) {
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
        String content = this.getOriginalContent().substring(2, this.getOriginalContent().length() - 2);
        return Setting.DISCORD_FORMAT_BOLD.getString().contains("%message%") ?
                Setting.DISCORD_FORMAT_BOLD.getString().replace("%message%", content) :
                Setting.DISCORD_FORMAT_BOLD.getString() + content + "&r";
    }
}

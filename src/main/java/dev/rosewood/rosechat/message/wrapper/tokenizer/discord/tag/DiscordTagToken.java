package dev.rosewood.rosechat.message.wrapper.tokenizer.discord.tag;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.hook.discord.DiscordChatProvider;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class DiscordTagToken extends Token {

    private final String tagged;
    private final boolean isRole;

    public DiscordTagToken(RoseSender sender, RoseSender viewer, String originalContent, String tagged) {
        this(sender, viewer, originalContent, tagged, false);
    }

    public DiscordTagToken(RoseSender sender, RoseSender viewer, String originalContent, String tagged, boolean isRole) {
        super(sender, viewer, originalContent);
        this.tagged = tagged;
        this.isRole = isRole;
    }

    @Override
    public BaseComponent[] toComponents() {
        return TextComponent.fromLegacyText(this.asString());
    }

    @Override
    public String asString() {
        DiscordChatProvider discord = RoseChatAPI.getInstance().getDiscord();
        return "@" + (this.isRole ? discord.getRoleFromId(this.tagged).replace(" ", "_") : discord.getUserFromId(this.tagged));
    }
}

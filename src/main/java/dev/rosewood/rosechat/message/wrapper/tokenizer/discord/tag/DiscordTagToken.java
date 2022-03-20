package dev.rosewood.rosechat.message.wrapper.tokenizer.discord.tag;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.Group;
import dev.rosewood.rosechat.chat.Tag;
import dev.rosewood.rosechat.hook.discord.DiscordChatProvider;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import dev.rosewood.rosechat.message.wrapper.tokenizer.tag.TagToken;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class DiscordTagToken extends Token {

    private final MessageWrapper messageWrapper;
    private final Group group;
    private final String tagged;
    private final boolean isRole;

    public DiscordTagToken(MessageWrapper messageWrapper, Group group, RoseSender sender, RoseSender viewer, String originalContent, String tagged) {
        this(messageWrapper, group, sender, viewer, originalContent, tagged, false);
    }

    public DiscordTagToken(MessageWrapper messageWrapper, Group group, RoseSender sender, RoseSender viewer, String originalContent, String tagged, boolean isRole) {
        super(sender, viewer, originalContent);
        this.messageWrapper = messageWrapper;
        this.group = group;
        this.tagged = tagged;
        this.isRole = isRole;
    }

    @Override
    public BaseComponent[] toComponents() {
        DiscordChatProvider discord = RoseChatAPI.getInstance().getDiscord();
        Player player = Bukkit.getPlayer(discord.getUserFromId(this.tagged));
        String taggedName = discord.getUserFromId(this.tagged);
        String prefix = "@";

        for (Tag tag : RoseChatAPI.getInstance().getTags()) {
            if (tag.getPrefix().equals(prefix)) {
                if (!tag.shouldTagOnlinePlayers() || !tag.getPrefix().equals(prefix)) continue;
                this.messageWrapper.setTagSound(tag.getSound());
                if (player == null) break;
                return new TagToken(this.messageWrapper, this.group, this.getSender(), this.getViewer(), tag, this.getOriginalContent(),
                        tag.getPrefix() + taggedName + (tag.getSuffix() != null ? tag.getSuffix() : "")).toComponents();
            }
        }

        if (this.isRole) {
            this.messageWrapper.getTaggedPlayers().addAll(discord.getPlayersWithRole(this.tagged));
        }

        String finalTag = prefix + (this.isRole ? discord.getRoleFromId(this.tagged).replace(" ", "_") : discord.getUserFromId(this.tagged)) + "&f&r";

        ComponentBuilder componentBuilder = new ComponentBuilder();
        for (char c : finalTag.toCharArray()) {
            componentBuilder.append(String.valueOf(c), ComponentBuilder.FormatRetention.NONE);
        }
        return componentBuilder.create();
    }
}

package dev.rosewood.rosechat.hook.discord;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.Group;
import dev.rosewood.rosechat.listener.DiscordSRVListener;
import dev.rosewood.rosechat.manager.DiscordEmojiManager;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.placeholders.CustomPlaceholder;
import dev.rosewood.rosechat.placeholders.DiscordPlaceholder;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.EmbedType;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Emote;
import github.scarsz.discordsrv.dependencies.jda.api.entities.GuildChannel;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Member;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Role;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DiscordSRVProvider implements DiscordChatProvider {

    private final DiscordSRV discord;
    private final DiscordEmojiManager emojiManager;

    public DiscordSRVProvider() {
        this.discord = DiscordSRV.getPlugin();
        this.emojiManager = RoseChatAPI.getInstance().getDiscordEmojiManager();
    }

    @Override
    public void sendMessage(MessageWrapper messageWrapper, Group group, String channel) {
        TextChannel textChannel = this.discord.getDestinationTextChannelForGameChannelName(channel);
        if (textChannel == null) return;

        BaseComponent[] message;
        boolean hasMessagePlaceholder = false;
        StringPlaceholders placeholders = MessageUtils.getSenderViewerPlaceholders(messageWrapper.getSender(), messageWrapper.getSender(), group).build();
        DiscordPlaceholder placeholder = RoseChatAPI.getInstance().getPlaceholderManager().getDiscordPlaceholder();

        CustomPlaceholder textPlaceholder = placeholder.getPlaceholder("text");
        String text = textPlaceholder != null ? textPlaceholder.getText().parse(messageWrapper.getSender(), messageWrapper.getSender(), placeholders) : null;
        if (text != null && text.contains("{message}")) {
            message = messageWrapper.parseToDiscord(text, messageWrapper.getSender());
            text = TextComponent.toPlainText(message);
        }

        CustomPlaceholder urlPlaceholder = placeholder.getPlaceholder("url");
        String url = urlPlaceholder != null ? urlPlaceholder.getText().parse(messageWrapper.getSender(), messageWrapper.getSender(), placeholders) : null;

        CustomPlaceholder titlePlaceholder = placeholder.getPlaceholder("title");
        String title = titlePlaceholder != null ? titlePlaceholder.getText().parse(messageWrapper.getSender(), messageWrapper.getSender(), placeholders) : null;
        if (title != null && title.contains("{message}")) {
            message = messageWrapper.parseToDiscord(title, messageWrapper.getSender());
            title = TextComponent.toPlainText(message);
            hasMessagePlaceholder = true;
        }

        CustomPlaceholder descriptionPlaceholder = placeholder.getPlaceholder("description");
        String description = descriptionPlaceholder != null ? descriptionPlaceholder.getText().parse(messageWrapper.getSender(), messageWrapper.getSender(), placeholders) : null;
        if (description != null && description.contains("{message}")) {
            message = messageWrapper.parseToDiscord(description, messageWrapper.getSender());
            description = TextComponent.toPlainText(message);
            hasMessagePlaceholder = true;
        }

        CustomPlaceholder timestampPlaceholder = placeholder.getPlaceholder("timestamp");
        boolean timestamp = timestampPlaceholder != null && Boolean.parseBoolean(timestampPlaceholder.getText().parse(messageWrapper.getSender(), messageWrapper.getSender(), placeholders));

        CustomPlaceholder colorPlaceholder = placeholder.getPlaceholder("color");
        int color = colorPlaceholder != null ? Integer.parseInt(colorPlaceholder.getText().parse(messageWrapper.getSender(), messageWrapper.getSender(), placeholders)) : 0;

        CustomPlaceholder thumbnailPlaceholder = placeholder.getPlaceholder("thumbnail");
        String thumbnail = thumbnailPlaceholder != null? placeholders.apply(thumbnailPlaceholder.getText().parse(messageWrapper.getSender(), messageWrapper.getSender(), placeholders)) : null;

        CustomPlaceholder thumbnailWidthPlaceholder = placeholder.getPlaceholder("thumbnail-width");
        int thumbnailWidth = thumbnailWidthPlaceholder != null? Integer.parseInt(thumbnailWidthPlaceholder.getText().parse(messageWrapper.getSender(), messageWrapper.getSender(), placeholders)) : 128;

        CustomPlaceholder thumbnailHeightPlaceholder = placeholder.getPlaceholder("thumbnail-height");
        int thumbnailHeight = thumbnailHeightPlaceholder != null? Integer.parseInt(thumbnailHeightPlaceholder.getText().parse(messageWrapper.getSender(), messageWrapper.getSender(), placeholders)) : 128;

        if (hasMessagePlaceholder) {
            MessageEmbed messageEmbed = new MessageEmbed(url,
                    this.emojiManager.formatUnicode(title),
                    this.emojiManager.formatUnicode(description),
                    EmbedType.RICH,
                    timestamp ? OffsetDateTime.now(): null,
                    color,
                    new MessageEmbed.Thumbnail(thumbnail, thumbnail, thumbnailWidth, thumbnailHeight),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null);
            // Unfortunately, may not always be able to be deleted.
            textChannel.sendMessageEmbeds(messageEmbed).queue((m) -> {
                if (messageWrapper.getDeletableMessage() != null) messageWrapper.getDeletableMessage().setDiscordId(m.getId());
            });
        } else {
            if (text != null) textChannel.sendMessage(this.emojiManager.formatUnicode(text)).queue((m) -> {
                if (messageWrapper.getDeletableMessage() != null) messageWrapper.getDeletableMessage().setDiscordId(m.getId());
            });
        }
    }

    @Override
    public void deleteMessage(String id) {
        // Check every channel for the message (as we don't know where it came from) and delete it.
        for (String channel : this.discord.getChannels().keySet()) {
            this.discord.getDestinationTextChannelForGameChannelName(channel).deleteMessageById(id).queue();
        }
    }

    @Override
    public String getChannelName(String id) {
        GuildChannel channel = this.discord.getJda().getGuildChannelById(id);
        return channel == null ? null : channel.getName();
    }

    @Override
    public String getServerId() {
        return this.discord.getMainGuild().getId();
    }

    @Override
    public String getUserFromId(String id) {
        UUID uuid = this.discord.getAccountLinkManager().getUuid(id);
        Member member = this.discord.getMainGuild().getMemberById(id);
        if (member == null) return this.getRoleFromId(id);

        String color = DiscordSRVListener.getColor(member);
        return uuid == null ? "#" + color + member.getEffectiveName() : Bukkit.getOfflinePlayer(uuid).getName();
    }

    @Override
    public String getRoleFromId(String id) {
        Role role = this.discord.getMainGuild().getRoleById(id);
        if (role == null) return null;

        String color = Integer.toHexString(role.getColorRaw());
        return "#" + color + role.getName();
    }

    @Override
    public List<UUID> getPlayersWithRole(String id) {
        Role role = this.discord.getMainGuild().getRoleById(id);
        if (role == null) return null;

        List<UUID> players = new ArrayList<>();
        for (Member member : this.discord.getMainGuild().getMembers()) {
            if (member.getRoles().contains(role)) {
                UUID uuid = this.discord.getAccountLinkManager().getUuid(member.getId());
                if (uuid != null) players.add(uuid);
            }
        }

        return players;
    }

    @Override
    public String getCustomEmoji(String name) {
        List<Emote> emotes = this.discord.getMainGuild().getEmotesByName(name, true);
        return (emotes == null || emotes.isEmpty()) ? null : emotes.get(0).getAsMention();
    }

    @Override
    public String getUserTag(String name) {
        String tag = "";
        List<Member> members = this.discord.getMainGuild().getMembers();
        for (Member member : members)
            if (member.getEffectiveName().toLowerCase().contains(name.toLowerCase())) tag = member.getAsMention();

        return tag;
    }
}

package dev.rosewood.rosechat.hook.discord;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.listener.DiscordSRVListener;
import dev.rosewood.rosechat.manager.DiscordEmojiManager;
import dev.rosewood.rosechat.message.DeletableMessage;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosechat.placeholders.DiscordPlaceholder;
import dev.rosewood.rosechat.placeholders.condition.PlaceholderCondition;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.EmbedType;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Emote;
import github.scarsz.discordsrv.dependencies.jda.api.entities.GuildChannel;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Member;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Role;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import java.awt.Color;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class DiscordSRVProvider implements DiscordChatProvider {

    private final DiscordSRV discord;
    private final DiscordEmojiManager emojiManager;

    public DiscordSRVProvider() {
        this.discord = DiscordSRV.getPlugin();
        this.emojiManager = RoseChatAPI.getInstance().getDiscordEmojiManager();
    }

    @Override
    public void sendMessage(RoseMessage roseMessage, Channel group, String channel) {
        TextChannel textChannel = this.discord.getDestinationTextChannelForGameChannelName(channel);
        if (textChannel == null) return;

        boolean sendAsEmbed = false;
        StringPlaceholders placeholders = MessageUtils.getSenderViewerPlaceholders(roseMessage.getSender(), roseMessage.getSender(), group).build();
        DiscordPlaceholder placeholder = RoseChatAPI.getInstance().getPlaceholderManager().getDiscordPlaceholder();

        PlaceholderCondition textPlaceholder = placeholder.getPlaceholder("text");
        String text = textPlaceholder != null ? textPlaceholder.parseToString(roseMessage.getSender(), roseMessage.getSender(), placeholders) : null;
        if (text != null) {
            BaseComponent[] message = roseMessage.parseMessageToDiscord(roseMessage.getSender(), text).components();
            text = MessageUtils.processForDiscord(TextComponent.toLegacyText(message));
        }

        PlaceholderCondition urlPlaceholder = placeholder.getPlaceholder("url");
        String url = urlPlaceholder != null ?
                ChatColor.stripColor(placeholders.apply(urlPlaceholder.parseToString(roseMessage.getSender(), roseMessage.getSender(), placeholders))) : null;

        PlaceholderCondition titlePlaceholder = placeholder.getPlaceholder("title");
        String title = titlePlaceholder != null ?
                placeholders.apply(titlePlaceholder.parseToString(roseMessage.getSender(), roseMessage.getSender(), placeholders)) : null;
        if (title != null) {
            BaseComponent[] message = roseMessage.parseMessageToDiscord(roseMessage.getSender(), title).components();
            title = MessageUtils.processForDiscord(TextComponent.toLegacyText(message));
            sendAsEmbed = true;
        }

        PlaceholderCondition descriptionPlaceholder = placeholder.getPlaceholder("description");
        String description = descriptionPlaceholder != null ?
                placeholders.apply(descriptionPlaceholder.parseToString(roseMessage.getSender(), roseMessage.getSender(), placeholders)) : null;
        if (description != null) {
            BaseComponent[] message = roseMessage.parseMessageToDiscord(roseMessage.getSender(), description).components();
            description = MessageUtils.processForDiscord(TextComponent.toLegacyText(message));
            sendAsEmbed = true;
        }

        PlaceholderCondition timestampPlaceholder = placeholder.getPlaceholder("timestamp");
        boolean timestamp = timestampPlaceholder != null && Boolean.parseBoolean(timestampPlaceholder.parseToString(roseMessage.getSender(), roseMessage.getSender(), placeholders));

        PlaceholderCondition colorPlaceholder = placeholder.getPlaceholder("color");
        int color = 16777215; // #FFFFFF
        if (colorPlaceholder != null) {
            String colorStr = colorPlaceholder.parseToString(roseMessage.getSender(), roseMessage.getSender(), placeholders);
            if (colorStr.startsWith("#")) {
                Color c = Color.decode(colorStr);
                color = (c.getRed() << 16) + (c.getGreen() << 8) + c.getBlue();
            } else {
                color = Integer.parseInt(colorStr);
            }
        }

        PlaceholderCondition thumbnailPlaceholder = placeholder.getPlaceholder("thumbnail");
        String thumbnail = thumbnailPlaceholder != null? placeholders.apply(thumbnailPlaceholder.parseToString(roseMessage.getSender(), roseMessage.getSender(), placeholders)) : null;

        PlaceholderCondition thumbnailWidthPlaceholder = placeholder.getPlaceholder("thumbnail-width");
        int thumbnailWidth = thumbnailWidthPlaceholder != null? Integer.parseInt(thumbnailWidthPlaceholder.parseToString(roseMessage.getSender(), roseMessage.getSender(), placeholders)) : 128;

        PlaceholderCondition thumbnailHeightPlaceholder = placeholder.getPlaceholder("thumbnail-height");
        int thumbnailHeight = thumbnailHeightPlaceholder != null? Integer.parseInt(thumbnailHeightPlaceholder.parseToString(roseMessage.getSender(), roseMessage.getSender(), placeholders)) : 128;

        if (sendAsEmbed) {
            MessageEmbed messageEmbed = new MessageEmbed(url,
                    this.emojiManager.formatUnicode(ChatColor.stripColor(title)),
                    this.emojiManager.formatUnicode(ChatColor.stripColor(description)),
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
                this.updateMessageLogs(roseMessage.getUUID(), m.getId());
            });
        } else if (text != null) {
            discord.processChatMessage(roseMessage.getSender().asPlayer(), this.emojiManager.formatUnicode(text), channel, false);
        }
    }

    private void updateMessageLogs(UUID minecraftId, String discordId) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData data = RoseChatAPI.getInstance().getPlayerData(player.getUniqueId());
            for (DeletableMessage message : data.getMessageLog().getDeletableMessages()) {
                if (message.getUUID().equals(minecraftId)) message.setDiscordId(discordId);
            }
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
    public String getChannelFromName(String name) {
        GuildChannel channel = this.discord.getJda().getTextChannelsByName(name, true).get(0);
        return channel == null ? null : channel.getAsMention();
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
        return uuid == null ? "#" + (color.length() == 5 ? "0" + color : color) + member.getEffectiveName() : Bukkit.getOfflinePlayer(uuid).getName();
    }

    @Override
    public String getRoleFromId(String id) {
        Role role = this.discord.getMainGuild().getRoleById(id);
        if (role == null) return null;

        String color = "FFFFFF";
        if (role.getColor() != null) Integer.toHexString(role.getColorRaw());
        return "#" + (color.length() == 5 ? "0" + color : color) + role.getName();
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
        return (emotes == null || emotes.isEmpty()) ? ":" + name + ":" : emotes.get(0).getAsMention();
    }

    @Override
    public String getUserTag(String name) {
        String tag = "";
        List<Member> members = this.discord.getMainGuild().getMembers();
        for (Member member : members)
            if (member.getEffectiveName().toLowerCase().contains(name.toLowerCase())) tag = member.getAsMention();

        return tag;
    }

    @Override
    public DetectedMention matchPartialMember(String input) {
        for (Member member : this.discord.getMainGuild().getMembers()) {
            int matchLength = this.getMatchLength(input, member.getEffectiveName());
            if (matchLength != -1)
                return new DetectedMention(this.getUserFromId(member.getId()), member.getAsMention(), matchLength);
        }

        return null;
    }

    @Override
    public DetectedMention matchPartialChannel(String input) {
        for (GuildChannel channel : this.discord.getMainGuild().getChannels()) {
            int matchLength = this.getMatchLength(input, channel.getName());
            if (matchLength != -1)
                return new DetectedMention(this.getChannelFromName(channel.getName()), channel.getAsMention(), matchLength);
        }

        return null;
    }

    private int getMatchLength(String input, String memberName) {
        int matchLength = 0;
        for (int i = 0, j = 0; i < input.length() && j < memberName.length(); i++, j++) {
            int inputChar = Character.toUpperCase(input.codePointAt(i));
            int memberChar = Character.toUpperCase(memberName.codePointAt(j));
            if (inputChar == memberChar) {
                matchLength++;
            } else if (i > 0 && (Character.isSpaceChar(inputChar) || Pattern.matches(MessageUtils.PUNCTUATION_REGEX, String.valueOf(Character.toChars(inputChar))))) {
                return matchLength;
            } else {
                return -1;
            }
        }

        return matchLength;
    }

}

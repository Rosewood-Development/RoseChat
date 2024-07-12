package dev.rosewood.rosechat.hook.discord;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.api.event.message.discord.PostParseDiscordMessageEvent;
import dev.rosewood.rosechat.api.event.message.discord.PreParseDiscordMessageEvent;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.listener.DiscordSRVListener;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.manager.DiscordEmojiManager;
import dev.rosewood.rosechat.message.DeletableMessage;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosechat.placeholder.CustomPlaceholder;
import dev.rosewood.rosechat.placeholder.DiscordEmbedPlaceholder;
import dev.rosewood.rosechat.placeholder.DefaultPlaceholders;
import dev.rosewood.rosechat.placeholder.condition.PlaceholderCondition;
import dev.rosewood.rosegarden.hook.PlaceholderAPIHook;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.EmbedType;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Emote;
import github.scarsz.discordsrv.dependencies.jda.api.entities.GuildChannel;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Member;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Role;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import java.awt.Color;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
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
        if (textChannel == null)
            return;

        PreParseDiscordMessageEvent preParseDiscordMessageEvent = new PreParseDiscordMessageEvent(roseMessage, textChannel);
        Bukkit.getPluginManager().callEvent(preParseDiscordMessageEvent);
        if (preParseDiscordMessageEvent.isCancelled())
            return;

        StringPlaceholders placeholders = DefaultPlaceholders.getFor(roseMessage.getSender(), roseMessage.getSender(), group).build();
        String format = group.getSettings().getFormats().get("minecraft-to-discord");
        DiscordEmbedPlaceholder embedPlaceholder = RoseChatAPI.getInstance().getPlaceholderManager().getDiscordEmbedPlaceholders().get(format.substring(1, format.length() - 1));
        if (embedPlaceholder != null) {
            this.sendMessageEmbed(roseMessage, textChannel, embedPlaceholder, placeholders);
        } else {
            String text = roseMessage.parseMessageToDiscord(roseMessage.getSender(), group.getSettings().getFormats().get("minecraft-to-discord")).content();
            if (text == null)
                return;

            PostParseDiscordMessageEvent postParseDiscordMessageEvent = new PostParseDiscordMessageEvent(roseMessage, textChannel, text);
            Bukkit.getPluginManager().callEvent(postParseDiscordMessageEvent);

            if (postParseDiscordMessageEvent.isCancelled())
                return;

            text = postParseDiscordMessageEvent.getOutput();

            if (Setting.SUPPORT_THIRD_PARTY_PLUGINS.getBoolean()) {
                this.discord.processChatMessage(roseMessage.getSender().asPlayer(), this.emojiManager.formatUnicode(text), channel, false);
            } else {
                textChannel.sendMessage(this.emojiManager.formatUnicode(text)).queue((message) -> {
                    this.updateMessageLogs(roseMessage.getUUID(), message.getId());
                });
            }
        }
    }

    private void sendMessageEmbed(RoseMessage roseMessage, TextChannel channel, DiscordEmbedPlaceholder embedPlaceholder, StringPlaceholders placeholders) {
        // Title
        PlaceholderCondition placeholderCondition = embedPlaceholder.get("title");
        String title = placeholderCondition  != null ?
                ChatColor.stripColor(placeholders.apply(placeholderCondition.parseToString(roseMessage.getSender(), roseMessage.getSender(), placeholders))) :
                null;

        if (title != null)
            title = roseMessage.parseMessageToDiscord(roseMessage.getSender(), title).content();

        // Description
        placeholderCondition = embedPlaceholder.get("description");
        String description = placeholderCondition != null ?
                ChatColor.stripColor(placeholders.apply(placeholderCondition.parseToString(roseMessage.getSender(), roseMessage.getSender(), placeholders))) :
                null;

        if (description != null)
            description = roseMessage.parseMessageToDiscord(roseMessage.getSender(), description).content();

        PostParseDiscordMessageEvent postParseDiscordMessageEvent = new PostParseDiscordMessageEvent(roseMessage, channel,
                title != null && title.contains("{message}") ? title : description);
        Bukkit.getPluginManager().callEvent(postParseDiscordMessageEvent);

        if (postParseDiscordMessageEvent.isCancelled())
            return;

        if (title != null && title.contains("{message}")) {
            title = postParseDiscordMessageEvent.getOutput();
        } else {
            description = postParseDiscordMessageEvent.getOutput();
        }

        // URL
        placeholderCondition = embedPlaceholder.get("url");
        String url = parsePlaceholder(placeholderCondition, roseMessage.getSender(), placeholders);
        if (url != null)
            url = ChatColor.stripColor(PlaceholderAPIHook.applyPlaceholders(roseMessage.getSender().asPlayer(), url));

        // Color
        placeholderCondition = embedPlaceholder.get("color");
        int color = 16777215;
        if (placeholderCondition != null) {
            String colorStr = placeholderCondition.parseToString(roseMessage.getSender(), roseMessage.getSender(), placeholders);
            if (colorStr.startsWith("#")) {
                Color c = Color.decode(colorStr);
                color = (c.getRed() << 16) + (c.getGreen() << 8) + c.getBlue();
            } else {
                color = Integer.parseInt(colorStr);
            }
        }

        // Timestamp
        placeholderCondition = embedPlaceholder.get("timestamp");
        String timestampStr = parsePlaceholder(placeholderCondition, roseMessage.getSender(), placeholders);
        boolean timestamp = Boolean.parseBoolean(timestampStr);

        // Image
        placeholderCondition = embedPlaceholder.get("image.url");
        String imageUrl = parsePlaceholder(placeholderCondition, roseMessage.getSender(), placeholders);
        if (imageUrl != null)
            imageUrl = ChatColor.stripColor(PlaceholderAPIHook.applyPlaceholders(roseMessage.getSender().asPlayer(), imageUrl));

        placeholderCondition = embedPlaceholder.get("image.height");
        String imageHeightStr = parsePlaceholder(placeholderCondition, roseMessage.getSender(), placeholders);
        int imageHeight = imageHeightStr != null ? Integer.parseInt(imageHeightStr) : 128;

        placeholderCondition = embedPlaceholder.get("image.width");
        String imageWidthStr = parsePlaceholder(placeholderCondition, roseMessage.getSender(), placeholders);
        int imageWidth = imageWidthStr != null ? Integer.parseInt(imageWidthStr) : 128;

        // Thumbnail
        placeholderCondition = embedPlaceholder.get("thumbnail.url");
        String thumbnailUrl = parsePlaceholder(placeholderCondition, roseMessage.getSender(), placeholders);
        if (thumbnailUrl != null)
            thumbnailUrl = ChatColor.stripColor(PlaceholderAPIHook.applyPlaceholders(roseMessage.getSender().asPlayer(), thumbnailUrl));

        placeholderCondition = embedPlaceholder.get("thumbnail.height");
        String thumbnailHeightStr = parsePlaceholder(placeholderCondition, roseMessage.getSender(), placeholders);
        int thumbnailHeight = thumbnailHeightStr != null ? Integer.parseInt(thumbnailHeightStr) : 128;

        placeholderCondition = embedPlaceholder.get("thumbnail.width");
        String thumbnailWidthStr = parsePlaceholder(placeholderCondition, roseMessage.getSender(), placeholders);
        int thumbnailWidth = thumbnailWidthStr != null ? Integer.parseInt(thumbnailWidthStr) : 128;

        // Author
        placeholderCondition = embedPlaceholder.get("author.name");
        String authorName = parsePlaceholder(placeholderCondition, roseMessage.getSender(), placeholders);
        if (authorName != null)
            authorName = ChatColor.stripColor(PlaceholderAPIHook.applyPlaceholders(roseMessage.getSender().asPlayer(), authorName));

        placeholderCondition = embedPlaceholder.get("author.url");
        String authorUrl = parsePlaceholder(placeholderCondition, roseMessage.getSender(), placeholders);
        if (authorUrl != null)
            authorUrl = ChatColor.stripColor(PlaceholderAPIHook.applyPlaceholders(roseMessage.getSender().asPlayer(), authorUrl));

        placeholderCondition = embedPlaceholder.get("author.icon-url");
        String authorIconUrl = parsePlaceholder(placeholderCondition, roseMessage.getSender(), placeholders);
        if (authorIconUrl != null)
            authorIconUrl = ChatColor.stripColor(PlaceholderAPIHook.applyPlaceholders(roseMessage.getSender().asPlayer(), authorIconUrl));

        // Footer
        placeholderCondition = embedPlaceholder.get("footer.text");
        String footerText = parsePlaceholder(placeholderCondition, roseMessage.getSender(), placeholders);
        if (footerText != null)
            footerText = ChatColor.stripColor(PlaceholderAPIHook.applyPlaceholders(roseMessage.getSender().asPlayer(), footerText));

        placeholderCondition = embedPlaceholder.get("footer.icon-url");
        String footerIconUrl = parsePlaceholder(placeholderCondition, roseMessage.getSender(), placeholders);
        if (footerIconUrl != null)
            footerIconUrl = ChatColor.stripColor(PlaceholderAPIHook.applyPlaceholders(roseMessage.getSender().asPlayer(), footerIconUrl));

        List<MessageEmbed.Field> fields = new LinkedList<>();
        for (CustomPlaceholder fieldPlaceholder : embedPlaceholder.getFields()) {
            PlaceholderCondition fieldCondition = fieldPlaceholder.get("name");
            String name = parsePlaceholder(fieldCondition, roseMessage.getSender(), placeholders);
            if (name != null)
                name = ChatColor.stripColor( PlaceholderAPIHook.applyPlaceholders(roseMessage.getSender().asPlayer(), name));

            fieldCondition = fieldPlaceholder.get("value");
            String value = parsePlaceholder(fieldCondition, roseMessage.getSender(), placeholders);
            if (value != null)
                value = ChatColor.stripColor(PlaceholderAPIHook.applyPlaceholders(roseMessage.getSender().asPlayer(), value));

            fieldCondition = fieldPlaceholder.get("inline");
            String inlineStr = parsePlaceholder(fieldCondition, roseMessage.getSender(), placeholders);
            boolean inline = Boolean.parseBoolean(inlineStr);

            fields.add(new MessageEmbed.Field(name, value, inline));
        }

        MessageEmbed messageEmbed = new MessageEmbed(
                url,
                title,
                description,
                EmbedType.RICH,
                timestamp ? OffsetDateTime.now() : null,
                color,
                thumbnailUrl == null ? null : new MessageEmbed.Thumbnail(thumbnailUrl, thumbnailUrl, thumbnailWidth, thumbnailHeight),
                null,
                authorName == null ? null : new MessageEmbed.AuthorInfo(authorName, authorUrl, authorIconUrl, authorIconUrl),
                null,
                footerText == null ? null : new MessageEmbed.Footer(footerText, footerIconUrl, footerIconUrl),
                imageUrl == null ? null : new MessageEmbed.ImageInfo(imageUrl, imageUrl, imageWidth, imageHeight),
                fields);
        channel.sendMessageEmbeds(messageEmbed).queue((message) -> {
            this.updateMessageLogs(roseMessage.getUUID(), message.getId());
        });
    }

    private String parsePlaceholder(PlaceholderCondition condition, RosePlayer rosePlayer, StringPlaceholders placeholders) {
        return condition != null ?
                placeholders.apply(condition.parseToString(rosePlayer, rosePlayer, placeholders)) :
                null;
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
        if (member == null)
            return this.getRoleFromId(id);

        String color = DiscordSRVListener.getColor(member);
        return uuid == null ? "#" + (color.length() == 5 ? "0" + color : color) + member.getEffectiveName() : Bukkit.getOfflinePlayer(uuid).getName();
    }

    @Override
    public String getRoleFromId(String id) {
        Role role = this.discord.getMainGuild().getRoleById(id);
        if (role == null)
            return null;

        String color = "FFFFFF";
        if (role.getColor() != null)
            return "#" + Integer.toHexString(role.getColorRaw()) + role.getName().replace(" ", "_");

        return "#" + (color.length() == 5 ? "0" + color : color) + role.getName().replace(" ", "_");
    }

    @Override
    public List<UUID> getPlayersWithRole(String id) {
        Role role = this.discord.getMainGuild().getRoleById(id);
        if (role == null)
            return null;

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

        // Attempt to match linked accounts if the Discord username is invalid.
        for (String accountId : this.discord.getAccountLinkManager().getLinkedAccounts().keySet()) {
            UUID uuid = this.discord.getAccountLinkManager().getLinkedAccounts().get(accountId);
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);

            if (player == null)
                break;

            int matchLength = this.getMatchLength(input, player.getName());
            if (matchLength != -1) {
                Member member = this.discord.getMainGuild().getMemberById(accountId);
                return new DetectedMention(this.getUserFromId(accountId), member.getAsMention(), matchLength);
            }

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

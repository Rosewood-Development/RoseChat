package dev.rosewood.rosechat.message.wrapper;

import dev.rosewood.rosechat.api.event.PostParseMessageEvent;
import dev.rosewood.rosechat.api.event.PreParseMessageEvent;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.message.DeletableMessage;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.parser.BungeeParser;
import dev.rosewood.rosechat.message.parser.FromDiscordParser;
import dev.rosewood.rosechat.message.parser.MessageParser;
import dev.rosewood.rosechat.message.parser.RoseChatParser;
import dev.rosewood.rosechat.message.parser.ToDiscordParser;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import java.util.UUID;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;

/**
 * A wrapper for chat messages which can be used to parse a message for a given receiver.
 */
public class RoseMessage {

    private UUID uuid;
    private final RosePlayer sender;
    private final MessageLocation location;
    private String playerInput;
    private Channel channel;
    private StringPlaceholders placeholders;

    /**
     * Creates a new RoseMessage to be parsed later.
     * This should be parsed using {@link #parse(RosePlayer, String)}.
     * @param sender The {@link RosePlayer} who is sending the message.
     * @param channel The {@link Channel} that the message is being sent in.
     */
    private RoseMessage(RosePlayer sender, Channel channel) {
        this(sender, MessageLocation.CHANNEL);
        this.channel = channel;
    }

    /**
     * Creates a new RoseMessage, using a {@link MessageLocation} instead of a {@link Channel}, to be parsed later.
     * This should be parsed using {@link #parse(RosePlayer, String)}.
     * @param sender The {@link RosePlayer} who is sending the message.
     * @param location The {@link MessageLocation} that the message is being sent in.
     */
    private RoseMessage(RosePlayer sender, MessageLocation location) {
        this.sender = sender;
        this.location = location;
        this.uuid = UUID.randomUUID();
    }

    /**
     * Parses the message.
     * This allows for the message to gain hover and click events, along with emojis and other features.
     * @param parser A {@link MessageParser} which will handle how the message gets parsed.
     *               Use {@link #parse(RosePlayer, String)} to parse without a {@link MessageParser}.
     * @param viewer The {@link RosePlayer} who will be viewing the message.
     * @param format The chat format for the message. If null, the message will be parsed without a format.
     * @param discordId The discord id of the message, or null if not sent from discord.
     * @return A {@link RoseMessageComponents} containing the parsed message.
     */
    public RoseMessageComponents parse(MessageParser parser, RosePlayer viewer, String format, String discordId, MessageRules messageRules) {
        // Call the PreParseMessageEvent and check if the message can still be parsed.
        PreParseMessageEvent preParseMessageEvent = new PreParseMessageEvent(this, viewer);
        Bukkit.getPluginManager().callEvent(preParseMessageEvent);

        if (preParseMessageEvent.isCancelled()) return null;
        RoseMessageComponents components = parser.parse(this, viewer, format);

        // Only call the PostParseMessageEvent if the message has player input.
        // This prevents non-chat messages from having delete buttons.
        if (this.playerInput != null) {
            PostParseMessageEvent postParseMessageEvent = new PostParseMessageEvent(this, viewer, parser.getMessageDirection(), components);
            Bukkit.getPluginManager().callEvent(postParseMessageEvent);
            if (preParseMessageEvent.isCancelled()) return null;
            components = postParseMessageEvent.getMessageComponents();

            if (viewer != null) {
                PlayerData viewerData = viewer.getPlayerData();
                if (viewerData != null && messageRules != null && !messageRules.isIgnoringMessageLogging()) {
                    DeletableMessage deletableMessage = new DeletableMessage(this.uuid);
                    deletableMessage.setJson(ComponentSerializer.toString(components.components()));
                    deletableMessage.setClient(false);
                    deletableMessage.setDiscordId(discordId);
                    viewerData.getMessageLog().addDeletableMessage(deletableMessage);
                }
            }
        }

        return components;
    }

    public RoseMessageComponents parse(MessageParser parser, RosePlayer viewer, String format, String discordId) {
        return this.parse(parser, viewer, format, discordId, new MessageRules());
    }

    /**
     * Parses the message.
     * This allows for the message to gain hover and click events, along with emojis and other features.
     * @param viewer The {@link RosePlayer} who will be viewing the message.
     * @param format The chat format for the message. If null, the message will be parsed without a format.
     * @return A {@link RoseMessageComponents} containing the parsed message.
     */
    public RoseMessageComponents parse(RosePlayer viewer, String format) {
        RoseChatParser parser = new RoseChatParser();
        return this.parse(parser, viewer, format, null);
    }

    /**
     * Parses the message.
     * This allows for the message to gain hover and click events, along with emojis and other features.
     * This applies a parser to parse with specific settings for discord messages.
     * @param viewer The {@link RosePlayer} who will be viewing the message.
     * @param format The chat format for the message. If null, the message will be parsed without a format.
     * @param discordId The id of the discord message. Used for deleting discord messages.
     * @return A {@link RoseMessageComponents} containing the parsed message.
     */
    public RoseMessageComponents parseMessageFromDiscord(RosePlayer viewer, String format, String discordId) {
        FromDiscordParser parser = new FromDiscordParser();
        return this.parse(parser, viewer, format, discordId);
    }

    /**
     * Parses the message.
     * This allows for the message to gain hover and click events, along with emojis and other features.
     * This applies a parser to parse with specific settings for discord messages.
     * @param viewer The {@link RosePlayer} who will be viewing the message.
     * @param format The chat format for the message. If null, the message will be parsed without a format.
     * @return A {@link RoseMessageComponents} containing the parsed message.
     */
    public RoseMessageComponents parseMessageToDiscord(RosePlayer viewer, String format) {
        ToDiscordParser parser = new ToDiscordParser();
        return this.parse(parser, viewer, format, null);
    }

    /**
     * Parses the message.
     * This allows for the message to gain hover and click events, along with emojis and other features.
     * This applies a parser to parse with specific settings for bungee messages.
     * @param viewer The {@link RosePlayer} who will be viewing the message.
     * @param format The chat format for the message. If null, the message will be parsed without a format.
     * @return A {@link RoseMessageComponents} containing the parsed message.
     */
    public RoseMessageComponents parseBungeeMessage(RosePlayer viewer, String format) {
        BungeeParser parser = new BungeeParser();
        return this.parse(parser, viewer, format, null);
    }

    /**
     * @return The {@link RosePlayer} who sent the message.
     */
    public RosePlayer getSender() {
        return this.sender;
    }

    /**
     * @return The {@link PlayerData} of the {@link RosePlayer} who sent the message.
     */
    public PlayerData getSenderData() {
        return this.sender.getPlayerData();
    }

    /**
     * @return The message that should be parsed.
     */
    public String getPlayerInput() {
        return this.playerInput;
    }

    /**
     * Changes the message that should be parsed.
     * @param playerInput The new message to use.
     */
    public void setPlayerInput(String playerInput) {
        this.playerInput = playerInput;
    }

    /**
     * @return The {@link Channel} that the message was sent in.
     */
    public Channel getChannel() {
        return this.channel;
    }

    /**
     * @return The {@link MessageLocation} that the message was sent in.
     */
    public MessageLocation getLocation() {
        return this.location;
    }

    /**
     * Converts the location to a string to be used in permissions.
     * Only includes the last part of the permission. For example, 'channel.global'.
     * @return The permission for the location of the message.
     */
    public String getLocationPermission() {
        return switch (this.location) {
            case CHANNEL -> {
                if (this.channel == null) yield null;
                yield "channel." + this.channel.getId();
            }
            case GROUP -> "group";
            default -> this.location.toString().toLowerCase();
        };
    }

    /**
     * Sets the placeholders for this message.
     * @param placeholders The {@link StringPlaceholders} to use.
     */
    public void setPlaceholders(StringPlaceholders placeholders) {
        this.placeholders = placeholders;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * @return The {@link StringPlaceholders} that this message should use.
     */
    public StringPlaceholders getPlaceholders() {
        return this.placeholders;
    }

    public static RoseMessage forChannel(RosePlayer sender, Channel channel) {
        return new RoseMessage(sender, channel);
    }

    public static RoseMessage forLocation(RosePlayer sender, MessageLocation location) {
        return new RoseMessage(sender, location);
    }

}

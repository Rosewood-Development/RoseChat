package dev.rosewood.rosechat.message.wrapper;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.manager.DebugManager;
import dev.rosewood.rosechat.message.DeletableMessage;
import dev.rosewood.rosechat.message.PermissionArea;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.parser.BungeeParser;
import dev.rosewood.rosechat.message.parser.FromDiscordParser;
import dev.rosewood.rosechat.message.parser.MessageParser;
import dev.rosewood.rosechat.message.parser.RoseChatParser;
import dev.rosewood.rosechat.message.parser.ToDiscordParser;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import java.util.UUID;
import net.md_5.bungee.api.chat.BaseComponent;

/**
 * A wrapper for chat messages which can be used to parse a message for a given receiver.
 */
public class RoseMessage {

    private final RosePlayer sender;
    private final PermissionArea location;

    private UUID uuid;
    private String discordId;
    private String playerInput;
    private Channel channel;
    private StringPlaceholders placeholders;
    private boolean usePlayerChatColor;

    /**
     * Creates a new RoseMessage to be parsed later.
     * This should be parsed using {@link #parse(RosePlayer, String)}.
     * @param sender The {@link RosePlayer} who is sending the message.
     * @param channel The {@link Channel} that the message is being sent in.
     */
    private RoseMessage(RosePlayer sender, Channel channel) {
        this(sender, PermissionArea.CHANNEL);

        this.channel = channel;
    }

    /**
     * Creates a new RoseMessage, using a {@link PermissionArea} instead of a {@link Channel}, to be parsed later.
     * This should be parsed using {@link #parse(RosePlayer, String)}.
     * @param sender The {@link RosePlayer} who is sending the message.
     * @param location The {@link PermissionArea} that the message is being sent in.
     */
    private RoseMessage(RosePlayer sender, PermissionArea location) {
        this.sender = sender;
        this.location = location;
        this.uuid = UUID.randomUUID();
        this.usePlayerChatColor = true;
    }

    /**
     * Parses the message.
     * This allows for the message to gain hover and click events, along with emojis and other features.
     * @param parser A {@link MessageParser} which will handle how the message gets parsed.
     *               Use {@link #parse(RosePlayer, String)} to parse without a {@link MessageParser}.
     * @param viewer The {@link RosePlayer} who will be viewing the message.
     * @param format The chat format for the message. If null, the message will be parsed without a format.
     * @param discordId The discord id of the message, or null if not sent from discord.
     * @return A {@link MessageTokenizerResults} containing the parsed message.
     */
    public <T> MessageTokenizerResults<T> parse(MessageParser<T> parser, RosePlayer viewer, String format, String discordId) {
        this.discordId = discordId;

        DebugManager debugManager = RoseChat.getInstance().getManager(DebugManager.class);
        if (debugManager.isEnabled())
            debugManager.addMessage(() ->
                    "[" + parser.getClass().getSimpleName() + "] Starting to Parse Message: " +
                            (this.playerInput == null ? format : (this.playerInput + " with Format: " + format)));

        return parser.parse(this, viewer, format);
    }

    /**
     * Parses the message.
     * This allows for the message to gain hover and click events, along with emojis and other features.
     * @param viewer The {@link RosePlayer} who will be viewing the message.
     * @param format The chat format for the message. If null, the message will be parsed without a format.
     * @return A {@link MessageTokenizerResults} containing the parsed message.
     */
    public MessageTokenizerResults<BaseComponent[]> parse(RosePlayer viewer, String format) {
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
     * @return A {@link MessageTokenizerResults} containing the parsed message.
     */
    public MessageTokenizerResults<BaseComponent[]> parseMessageFromDiscord(RosePlayer viewer, String format, String discordId) {
        FromDiscordParser parser = new FromDiscordParser();
        return this.parse(parser, viewer, format, discordId);
    }

    /**
     * Parses the message.
     * This allows for the message to gain hover and click events, along with emojis and other features.
     * This applies a parser to parse with specific settings for discord messages.
     * @param viewer The {@link RosePlayer} who will be viewing the message.
     * @param format The chat format for the message. If null, the message will be parsed without a format.
     * @return A {@link MessageTokenizerResults} containing the parsed message.
     */
    public MessageTokenizerResults<String> parseMessageToDiscord(RosePlayer viewer, String format) {
        ToDiscordParser parser = new ToDiscordParser();
        return this.parse(parser, viewer, format, null);
    }

    /**
     * Parses the message.
     * This allows for the message to gain hover and click events, along with emojis and other features.
     * This applies a parser to parse with specific settings for bungee messages.
     * @param viewer The {@link RosePlayer} who will be viewing the message.
     * @param format The chat format for the message. If null, the message will be parsed without a format.
     * @return A {@link MessageTokenizerResults} containing the parsed message.
     */
    public MessageTokenizerResults<BaseComponent[]> parseBungeeMessage(RosePlayer viewer, String format) {
        BungeeParser parser = new BungeeParser();
        return this.parse(parser, viewer, format, null);
    }

    /**
     * Turns this message into a deletable message.
     * @param json The json to use, typically gotten from the PostParseMessageEvent.
     * @param discordId The discord id of the message.
     * @return The deletable message.
     */
    public DeletableMessage createDeletableMessage(String json, String discordId) {
        DeletableMessage deletableMessage = new DeletableMessage(this.getUUID());
        deletableMessage.setJson(json);
        deletableMessage.setClient(false);
        deletableMessage.setDiscordId(discordId);

        if (!this.sender.isConsole())
            deletableMessage.setSender(this.sender.getUUID());

        if (this.channel != null)
            deletableMessage.setChannel(this.channel.getId());

        return deletableMessage;
    }

    /**
     * @return The {@link RosePlayer} who sent the message.
     */
    public RosePlayer getSender() {
        return this.sender;
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
     * @return The {@link PermissionArea} that the message was sent in.
     */
    public PermissionArea getLocation() {
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

    public String getDiscordId() {
        return this.discordId;
    }

    public void setDiscordId(String discordId) {
        this.discordId = discordId;
    }

    public void setUsePlayerChatColor(boolean usePlayerChatColor) {
        this.usePlayerChatColor = usePlayerChatColor;
    }

    public boolean shouldUsePlayerChatColor() {
        return this.usePlayerChatColor;
    }
    
    /**
     * @return The {@link StringPlaceholders} that this message should use.
     */
    public StringPlaceholders getPlaceholders() {
        return this.placeholders == null ? StringPlaceholders.empty() : this.placeholders;
    }

    public static RoseMessage forChannel(RosePlayer sender, Channel channel) {
        return new RoseMessage(sender, channel);
    }

    public static RoseMessage forLocation(RosePlayer sender, PermissionArea location) {
        return new RoseMessage(sender, location);
    }
    
}

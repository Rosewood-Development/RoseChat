package dev.rosewood.rosechat.message.wrapper;

import com.google.common.base.Stopwatch;
import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.event.PostParseMessageEvent;
import dev.rosewood.rosechat.api.event.PreParseMessageEvent;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.message.DeletableMessage;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageLog;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.parser.MessageParser;
import dev.rosewood.rosechat.message.parser.RoseChatParser;
import dev.rosewood.rosechat.message.tokenizer.MessageOutputs;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;

/**
 * Wrapper to turn regular messages into BaseComponent[]'s with colour, emoji, tags, etc.
 */
public class RoseMessage {

    private UUID uuid;
    private DeletableMessage deletableMessage;

    // In Values
    private final RosePlayer sender;
    private final MessageLocation location;
    private String message;
    private Channel channel;
    private MessageRules messageRules;
    private StringPlaceholders placeholders;

    // Out Values
    private final MessageOutputs outputs = new MessageOutputs();
    private BaseComponent[] components;

    /**
     * Creates a new RoseMessage to be parsed later.
     * This should be parsed using {@link #parse(RosePlayer, String)}.
     * @param sender The {@link RosePlayer} who is sending the message.
     * @param channel The {@link Channel} that the message is being sent in.
     * @param message The message that is being sent.
     */
    public RoseMessage(RosePlayer sender, Channel channel, String message) {
        this(sender, MessageLocation.CHANNEL, message);
        this.channel = channel;
    }

    /**
     * Creates a new RoseMessage, using a {@link MessageLocation} instead of a {@link Channel}, to be parsed later.
     * This should be parsed using {@link #parse(RosePlayer, String)}.
     * @param sender The {@link RosePlayer} who is sending the message.
     * @param location The {@link MessageLocation} that the message is being sent in.
     * @param message The message that is being sent.
     */
    public RoseMessage(RosePlayer sender, MessageLocation location, String message) {
        this.sender = sender;
        this.location = location;
        this.message = message;

        this.uuid = UUID.randomUUID();
    }

    /**
     * Copy constructor to prevent asynchronously editing the tokenized value.
     * @param other The {@link RoseMessage} to copy.
     */
    public RoseMessage(RoseMessage other) {
        this.uuid = other.uuid;
        this.deletableMessage = null;
        this.sender = other.sender;
        this.location = other.location;
        this.message = other.message;
        this.channel = other.channel;
        this.messageRules = new MessageRules(other.messageRules);
        this.placeholders = other.placeholders;
    }

    /**
     * Applies the given message rules to this message.
     * @param messageRules The {@link MessageRules} to be applied.
     */
    public void applyRules(MessageRules messageRules) {
        this.messageRules = messageRules;
        messageRules.apply(this);
    }

    private void logMessage(MessageLog log, String discordId) {
        if (this.messageRules == null || this.messageRules.isIgnoringMessageLogging()) return;

        this.deletableMessage = new DeletableMessage(this.uuid);
        this.deletableMessage.setJson(ComponentSerializer.toString(this.components));
        this.deletableMessage.setClient(false);
        this.deletableMessage.setDiscordId(discordId);
        this.deletableMessage.setPrivateMessageInfo(this.messageRules.getPrivateMessageInfo());

        log.addDeletableMessage(this.deletableMessage);
    }

    /**
     * Parses the message.
     * This allows for the message to gain hover and click events, along with emojis and other features.
     * @param parser A {@link MessageParser} which will handle how the message gets parsed.
     *               Use {@link #parse(RosePlayer, String)} to parse without a {@link MessageParser}.
     * @param viewer The {@link RosePlayer} who will be viewing the message.
     * @param format The chat format for the message. If null, the message will be parsed without a format.
     * @param discordId The discord id of the message, or null if not sent from discord.
     * @return A {@link BaseComponent[]} containing the parsed message.
     */
    public BaseComponent[] parse(MessageParser parser, RosePlayer viewer, String format, String discordId) {
        // Call the PreParseMessageEvent and check if the message can still be parsed.
        PreParseMessageEvent preParseMessageEvent = new PreParseMessageEvent(this, viewer);
        Bukkit.getPluginManager().callEvent(preParseMessageEvent);

        if (preParseMessageEvent.isCancelled()) return null;
        this.components = parser.parse(this, this.sender, viewer, format);

        // Only call the PostParseMessageEvent if the message has a format.
        // This prevents non-chat messages from having delete buttons.
        if (format != null) {
            PostParseMessageEvent postParseMessageEvent = new PostParseMessageEvent(this, viewer, parser.getMessageDirection());
            Bukkit.getPluginManager().callEvent(postParseMessageEvent);
        }

        if (viewer != null) {
            PlayerData viewerData = viewer.getPlayerData();
            if (viewerData != null) this.logMessage(viewerData.getMessageLog(), discordId);
        }

        return this.components;
    }

    /**
     * Parses the message.
     * This allows for the message to gain hover and click events, along with emojis and other features.
     * @param viewer The {@link RosePlayer} who will be viewing the message.
     * @param format The chat format for the message. If null, the message will be parsed without a format.
     * @return A {@link BaseComponent[]} containing the parsed message.
     */
    public BaseComponent[] parse(RosePlayer viewer, String format) {
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
     * @return A {@link BaseComponent[]} containing the parsed message.
     */
    public BaseComponent[] parseMessageFromDiscord(RosePlayer viewer, String format, String discordId) {
//        FromDiscordParser parser = new FromDiscordParser();
//        return this.parse(parser, viewer, format, discordId);
        throw new UnsupportedOperationException();
    }

    /**
     * Parses the message.
     * This allows for the message to gain hover and click events, along with emojis and other features.
     * This applies a parser to parse with specific settings for discord messages.
     * @param viewer The {@link RosePlayer} who will be viewing the message.
     * @param format The chat format for the message. If null, the message will be parsed without a format.
     * @return A {@link BaseComponent[]} containing the parsed message.
     */
    public BaseComponent[] parseMessageToDiscord(RosePlayer viewer, String format) {
//        ToDiscordParser parser = new ToDiscordParser();
//        return this.parse(parser, viewer, format, null);
        throw new UnsupportedOperationException();
    }

    /**
     * Parses the message.
     * This allows for the message to gain hover and click events, along with emojis and other features.
     * This applies a parser to parse with specific settings for bungee messages.
     * @param viewer The {@link RosePlayer} who will be viewing the message.
     * @param format The chat format for the message. If null, the message will be parsed without a format.
     * @return A {@link BaseComponent[]} containing the parsed message.
     */
    public BaseComponent[] parseBungeeMessage(RosePlayer viewer, String format) {
//        BungeeParser parser = new BungeeParser();
//        return this.parse(parser, viewer, format, null);
        throw new UnsupportedOperationException();
    }

    /**
     * @return The {@link BaseComponent[]} containing the parsed message.
     * This will be null if the message as not been parsed.
     */
    public BaseComponent[] toComponents() {
        return this.components;
    }

    /**
     * Sets the components for the message.
     * This is mainly used for editing the message after it has been parsed.
     * @param components The components to use.
     */
    public void setComponents(BaseComponent[] components) {
        this.components = components;
    }

    /**
     * @return The {@link UUID} of the message.
     */
    public UUID getUUID() {
        return this.uuid;
    }

    /**
     * Sets the {@link UUID} of the message.
     * @param uuid The {@link UUID} to use.
     */
    public void setUUID(UUID uuid) {
        this.uuid = uuid;
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
    public String getMessage() {
        return this.message;
    }

    /**
     * Changes the message that should be parsed.
     * This will do nothing if the message has already been parsed.
     * @param message The new message to use.
     */
    protected void setMessage(String message) {
        this.message = message;
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
        switch (this.location) {
            case CHANNEL:
                if (this.channel == null) return null;
                return "channel." + this.channel.getId();
            case GROUP:
                return "group";
            default:
                return this.location.toString().toLowerCase();
        }
    }

    /**
     * @return The {@link MessageRules} that the message should abide by.
     */
    public MessageRules getMessageRules() {
        return this.messageRules;
    }

    /**
     * @return The {@link MessageOutputs} for this message.
     */
    public MessageOutputs getOutputs() {
        return this.outputs;
    }

    /**
     * @return The {@link DeletableMessage} version of this message.
     */
    public DeletableMessage getDeletableMessage() {
        return this.deletableMessage;
    }

    /**
     * Sets the placeholders for this message.
     * @param placeholders The {@link StringPlaceholders} to use.
     */
    public void setPlaceholders(StringPlaceholders placeholders) {
        this.placeholders = placeholders;
    }

    /**
     * @return The {@link StringPlaceholders} that this message should use.
     */
    public StringPlaceholders getPlaceholders() {
        return this.placeholders;
    }

}

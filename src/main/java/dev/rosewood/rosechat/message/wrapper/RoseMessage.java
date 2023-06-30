package dev.rosewood.rosechat.message.wrapper;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.api.event.message.PostParseMessageEvent;
import dev.rosewood.rosechat.api.event.message.PreParseMessageEvent;
import dev.rosewood.rosechat.chat.FilterType;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.message.DeletableMessage;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageLog;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.parser.BungeeParser;
import dev.rosewood.rosechat.message.parser.FromDiscordParser;
import dev.rosewood.rosechat.message.parser.MessageParser;
import dev.rosewood.rosechat.message.parser.RoseChatParser;
import dev.rosewood.rosechat.message.parser.ToDiscordParser;
import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.placeholders.RoseChatPlaceholder;
import dev.rosewood.rosegarden.hook.PlaceholderAPIHook;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;

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
    private final List<UUID> taggedPlayers;
    private Sound tagSound;
    private FilterType filterType;
    private boolean isBlocked;
    private BaseComponent[] tokenized;

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
        this.taggedPlayers = new ArrayList<>();
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
        this.taggedPlayers = new ArrayList<>();
        this.tagSound = null;
        this.filterType = null;
        this.isBlocked = false;
        this.tokenized = null;
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
        this.deletableMessage.setJson(ComponentSerializer.toString(this.tokenized));
        this.deletableMessage.setClient(false);
        this.deletableMessage.setDiscordId(discordId);
        this.deletableMessage.setPrivateMessageInfo(this.messageRules.getPrivateMessageInfo());

        log.addDeletableMessage(this.deletableMessage);
    }

    public String getChatColorFromFormat(RosePlayer viewer, String format) {
        if (format.equalsIgnoreCase("{message}")) return "";

        String lastColor = "";
        String lastFormat = "";

        String[] placeholders = format.split("\\{");
        String colorPlaceholder = placeholders.length > 2 ? placeholders[placeholders.length - 2] : placeholders[0];

        if (!colorPlaceholder.endsWith("}")) colorPlaceholder = colorPlaceholder.substring(0, colorPlaceholder.lastIndexOf("}") + 1);
        RoseChatPlaceholder placeholder = RoseChatAPI.getInstance().getPlaceholderManager().getPlaceholder(colorPlaceholder.substring(0, colorPlaceholder.length() - 1));
        String value = placeholder.getText().parseToString(this.sender, viewer, MessageUtils.getSenderViewerPlaceholders(this.sender, viewer, this.channel).build());

        if (this.sender.isPlayer())
            value = PlaceholderAPIHook.applyPlaceholders(this.sender.asPlayer(), value).replace(ChatColor.COLOR_CHAR, '&');

        Matcher colorMatcher = MessageUtils.STOP.matcher(value);
        while (colorMatcher.find())
            lastColor = colorMatcher.group();

        Matcher formatMatcher = MessageUtils.VALID_LEGACY_REGEX_FORMATTING.matcher(value);
        while (formatMatcher.find())
            lastFormat = formatMatcher.group();

        // Applies Placeholders in order to get any colour within them.
        if (this.sender.isPlayer())
            format = PlaceholderAPIHook.applyPlaceholders(this.sender.asPlayer(), format).replace(ChatColor.COLOR_CHAR, '&');

        // Check the format string for colours after, e.g. {player}:&c{message}
        colorMatcher = MessageUtils.STOP.matcher(format);
        while (colorMatcher.find()) {
            if (format.indexOf(colorMatcher.group()) > format.indexOf(colorPlaceholder)) lastColor = colorMatcher.group();
        }

        formatMatcher = MessageUtils.VALID_LEGACY_REGEX_FORMATTING.matcher(format);
        while (formatMatcher.find()) {
            if (format.indexOf(formatMatcher.group()) > format.indexOf(colorPlaceholder)) lastFormat = formatMatcher.group();
        }

        return HexUtils.colorify(lastFormat + lastColor);
    }

    /**
     * Parses the message using RoseChat's {@link MessageTokenizer}.
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
        this.tokenized = parser.parse(this, this.sender, viewer, format);

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

        return this.tokenized;
    }

    /**
     * Parses the message using RoseChat's {@link MessageTokenizer}.
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
     * Parses the message using RoseChat's {@link MessageTokenizer}.
     * This allows for the message to gain hover and click events, along with emojis and other features.
     * This applies a {@link FromDiscordParser} to parse with specific settings for discord messages.
     * @param viewer The {@link RosePlayer} who will be viewing the message.
     * @param format The chat format for the message. If null, the message will be parsed without a format.
     * @param discordId The id of the discord message. Used for deleting discord messages.
     * @return A {@link BaseComponent[]} containing the parsed message.
     */
    public BaseComponent[] parseMessageFromDiscord(RosePlayer viewer, String format, String discordId) {
        FromDiscordParser parser = new FromDiscordParser();
        return this.parse(parser, viewer, format, discordId);
    }

    /**
     * Parses the message using RoseChat's {@link MessageTokenizer}.
     * This allows for the message to gain hover and click events, along with emojis and other features.
     * This applies a {@link ToDiscordParser} to parse with specific settings for discord messages.
     * @param viewer The {@link RosePlayer} who will be viewing the message.
     * @param format The chat format for the message. If null, the message will be parsed without a format.
     * @return A {@link BaseComponent[]} containing the parsed message.
     */
    public BaseComponent[] parseMessageToDiscord(RosePlayer viewer, String format) {
        ToDiscordParser parser = new ToDiscordParser();
        return this.parse(parser, viewer, format, null);
    }

    /**
     * Parses the message using RoseChat's {@link MessageTokenizer}.
     * This allows for the message to gain hover and click events, along with emojis and other features.
     * This applies a {@link BungeeParser} to parse with specific settings for bungee messages.
     * @param viewer The {@link RosePlayer} who will be viewing the message.
     * @param format The chat format for the message. If null, the message will be parsed without a format.
     * @return A {@link BaseComponent[]} containing the parsed message.
     */
    public BaseComponent[] parseBungeeMessage(RosePlayer viewer, String format) {
        BungeeParser parser = new BungeeParser();
        return this.parse(parser, viewer, format, null);
    }

    /**
     * @return The {@link BaseComponent[]} containing the parsed message.
     * This will be null if the message as not been parsed.
     */
    public BaseComponent[] toComponents() {
        return this.tokenized;
    }

    /**
     * Sets the components for the message.
     * This is mainly used for editing the message after it has been parsed.
     * @param components The components to use.
     */
    public void setComponents(BaseComponent[] components) {
        this.tokenized = components;
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
     * @return The {@link FilterType} for the message.
     */
    public FilterType getFilterType() {
        return this.filterType;
    }

    /**
     * Sets the {@link FilterType} of the message.
     * This is used when deciding what to message the player.
     * @param filterType The {@link FilterType} for the message.
     */
    public void setFilterType(FilterType filterType) {
        this.filterType = filterType;
    }

    /**
     * @return True if the message should not be sent.
     */
    public boolean isBlocked() {
        return this.isBlocked;
    }

    /**
     * Sets whether this message should be blocked, and not sent later.
     * @param blocked True if the message should not be sent.
     */
    public void setBlocked(boolean blocked) {
        this.isBlocked = blocked;
    }

    /**
     * @return A list of players who have been tagged in the parsed message.
     */
    public List<UUID> getTaggedPlayers() {
        return this.taggedPlayers;
    }

    /**
     * @param tagSound The sound that should be played to the players who were tagged in the parsed message.
     */
    public void setTagSound(Sound tagSound) {
        this.tagSound = tagSound;
    }

    /**
     * @return The sound that should be played to the players who were tagged in the parsed message.
     */
    public Sound getTagSound() {
        return this.tagSound;
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

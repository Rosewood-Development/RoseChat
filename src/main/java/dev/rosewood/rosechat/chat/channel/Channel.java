package dev.rosewood.rosechat.chat.channel;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.hook.channel.ChannelProvider;
import dev.rosewood.rosechat.manager.ChannelManager;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class Channel {

    private final ChannelProvider provider;
    protected String id;
    protected final List<UUID> members;
    private boolean defaultChannel;
    private boolean muted;

    // Settings
    private String format;
    private String discordChannel;
    private List<String> commands;
    private List<String> overrideCommands;
    private List<String> shoutCommands;
    private String shoutFormat;
    private boolean sendBungeeMessagesToDiscord;

    public Channel(ChannelProvider provider) {
        this.members = new ArrayList<>();
        this.provider = provider;
        this.commands = new ArrayList<>();
        this.overrideCommands = new ArrayList<>();
        this.shoutCommands = new ArrayList<>();
    }

    /**
     * Called when a channel is loaded from a config file.
     */
    public void onLoad(String id, ConfigurationSection config) {
        this.id = id;
        if (config.contains("default") && config.getBoolean("default")) this.setDefault();
        if (config.contains("format")) this.setFormat(config.getString("format"));
        if (config.contains("discord")) this.setDiscordChannel(config.getString("discord"));
        if (config.contains("commands")) this.setCommands(config.getStringList("commands"));
        if (config.contains("override-commands")) this.setOverrideCommands(config.getStringList("override-commands"));
        if (config.contains("shout-commands")) this.setShoutCommands(config.getStringList("shout-commands"));
        if (config.contains("send-bungee-messages-to-discord")) this.setShouldSendBungeeMessagesToDiscord(config.getBoolean("send-bungee-messages-to-discord"));
        if (config.contains("shout-format")) this.setShoutFormat(config.getString("shout-format"));
    }

    /**
     * Called when a channel is loaded.
     */
    public void onLoad() {
        // No default implementation.
    }

    /**
     * Called when the player joins the server.
     * This is used to check if the player should join the channel when logging in
     * @param player The {@link Player} who is joining the channel.
     * @return True, if the player can join.
     */
    public boolean onLogin(Player player) {
        // No default implementation.
        return false;
    }

    /**
     * Called when the player enters a world.
     * This is used to check if the player should join the channel when changing world.
     * @param player The {@link Player} who is changing world.
     * @param from The {@link World} that the player was in.
     * @param to The {@link World} that the player is going to.
     * @return True, if the player should join the channel when entering the world.
     */
    public boolean onWorldJoin(Player player, World from, World to) {
        // No default implementation.
        return false;
    }

    /**
     * Called when the player leaves a world.
     * This is used to check if the player should leave the channel when changing world.
     * @param player The {@link Player} who is changing world.
     * @param from The {@link World} that the player was in.
     * @param to The {@link World} that the player is going to.
     * @return True, if the player should leave the channel when leaving the world.
     */
    public boolean onWorldLeave(Player player, World from, World to) {
        // No default implementation.
        return false;
    }

    /**
     * Called when a player joins the channel.
     * @param player The {@link Player} who is joining the channel.
     */
    public void onJoin(Player player) {
        this.members.add(player.getUniqueId());
    }

    /**
     * Called when a player leaves a channel.
     * @param player The {@link Player} who is leaving the channel.
     */
    public void onLeave(Player player) {
        this.members.remove(player.getUniqueId());
    }

    /**
     * Called when a message is sent to the channel.
     * @param sender The {@link RosePlayer} who is sending the message.
     * @param message The message to be sent.
     */
    public abstract void send(RosePlayer sender, String message);

    /**
     * Called when a message is sent to the channel.
     * @param sender The {@link RosePlayer} who is sending the message.
     * @param message The message to be sent.
     * @param format The format of the message.
     */
    public abstract void send(RosePlayer sender, String message, String format);

    /**
     * Called when a message is sent to the channel from Discord.
     * @param message The {@link RoseMessage} to be sent.
     * @param discordId The ID of the message sent in Discord.
     */
    public void send(RoseMessage message, String discordId) {
        // No default implementation
    }

    /**
     * Called when a message is sent from Bungee.
     * @param sender The {@link RosePlayer} who is sending the message.
     * @param message The message to be sent.
     * @param messageId The {@link UUID} of the message sent from the other server.
     * @param isJson True if the message contains a json string.
     *               This is typically only used when keep-format-over-bungee is enabled.
     */
    public void send(RosePlayer sender, String message, UUID messageId, boolean isJson) {
        // No default implementation
    }

    /**
     * Returns a list of members for the given channel.
     * These are the players who have joined the channel, not all the players receiving the message.
     * Use {@link #members} if a player does not need to be specified.
     * A player should be specified when getting a team from a supported plugin.
     * @return A list of UUIDs for the members of the channel.
     */
    public abstract List<UUID> getMembers();

    /**
     * @return The id of the channel.
     */
    public abstract String getId();

    /**
     * @return A list of servers linked to the channel.
     */
    public abstract List<String> getServers();

    /**
     * Called when a player uses a command to join the channel.
     * @param player The {@link Player} using the command.
     * @return True, if the player can join by using the command.
     */
    public abstract boolean canJoinByCommand(Player player);

    /**
     * @return The amount of members in the channel.
     */
    public int getMemberCount(RosePlayer sender) {
        return this.members.size();
    }

    /**
     * @param sender The {@link RosePlayer} getting the info.
     * @param trueValue A value representing 'true', e.g. &aTrue
     * @param falseValue A value representing 'false', e.g. &cFalse
     * @param nullValue A value representing 'null', e.g. &eNone
     * @return A {@link StringPlaceholders.Builder} containing values to be shown in the /chat info command.
     */
    public StringPlaceholders.Builder getInfoPlaceholders(RosePlayer sender, String trueValue, String falseValue, String nullValue) {
        return StringPlaceholders.builder()
                .add("default", this.isDefaultChannel() ? trueValue : falseValue)
                .add("muted", this.isMuted() ? trueValue : falseValue)
                .add("members", this.getMemberCount(sender))
                .add("players", this.getMemberCount(sender))
                .add("id", this.getId())
                .add("format", this.getFormat() == null ? nullValue : this.getFormat())
                .add("commands", this.commands.isEmpty() ? nullValue : this.getCommands().toString());
    }

    public ChannelProvider getProvider() {
        return this.provider;
    }

    /**
     * Floods the channel with the specified message.
     * @param message The message to use
     */
    public void send(String message) {
        // No default implementation.
    }

    /**
     * Checks if a message can be received by a player.
     * @param receiver The {@link Player} to receive the message.
     * @param data The {@link PlayerData} of the receiver.
     * @param senderUUID The {@link UUID} of the player sending the message.
     * @return True if the receiver can receive the message.
     */
    public boolean canPlayerReceiveMessage(RosePlayer receiver, PlayerData data, UUID senderUUID) {
        return (data != null
                && !data.getIgnoringPlayers().contains(senderUUID)
                && receiver.hasPermission("rosechat.channel." + this.getId())
                && (!data.isChannelHidden(this.getId()) || senderUUID == null || data.getUUID().equals(senderUUID)));
    }

    public boolean isDefaultChannel() {
        return this.defaultChannel;
    }

    public void setDefault() {
        RoseChat.getInstance().getManager(ChannelManager.class).setDefaultChannel(this);
        this.defaultChannel = true;
    }

    public boolean isMuted() {
        return this.muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    public String getFormat() {
        return this.format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getDiscordChannel() {
        return this.discordChannel;
    }

    public void setDiscordChannel(String discordChannel) {
        this.discordChannel = discordChannel;
    }

    public List<String> getCommands() {
        return this.commands;
    }

    public void setCommands(List<String> commands) {
        this.commands = commands;
    }

    public List<String> getOverrideCommands() {
        return this.overrideCommands;
    }

    public void setOverrideCommands(List<String> overrideCommands) {
        this.overrideCommands = overrideCommands;
    }

    public List<String> getShoutCommands() {
        return this.shoutCommands;
    }

    public void setShoutCommands(List<String> shoutCommands) {
        this.shoutCommands = shoutCommands;
    }

    public String getShoutFormat() {
        return this.shoutFormat;
    }

    public void setShoutFormat(String shoutFormat) {
        this.shoutFormat = shoutFormat;
    }

    public boolean shouldSendBungeeMessagesToDiscord() {
        return this.sendBungeeMessagesToDiscord;
    }

    public void setShouldSendBungeeMessagesToDiscord(boolean sendBungeeMessagesToDiscord) {
        this.sendBungeeMessagesToDiscord = sendBungeeMessagesToDiscord;
    }

    /**
     * Finds the next channel that the player should be moved to.
     * This is mainly used in instances where a player is removed from a channel and needs to join the
     * appropriate channel based on their location.
     * @param player The {@link Player} to move channels.
     * @return The {@link Channel} that the player should move to.
     */
    public static Channel findNextChannel(Player player) {
        RoseChatAPI api = RoseChatAPI.getInstance();

        // Find the correct channel for the player to go in.
        Channel foundChannel = null;
        for (Channel channel : api.getChannels()) {
            if (channel.onWorldJoin(player, null, player.getWorld())) {
                foundChannel = channel;
            }
        }

        if (foundChannel == null) foundChannel = api.getDefaultChannel();
        return foundChannel;
    }
}

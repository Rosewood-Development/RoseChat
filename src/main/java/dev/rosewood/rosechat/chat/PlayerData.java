package dev.rosewood.rosechat.chat;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.manager.DataManager;
import dev.rosewood.rosechat.message.MessageLog;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerData {

    private UUID uuid;
    private MessageLog messageLog;
    private String replyTo;
    private boolean messageSpy;
    private boolean channelSpy;
    private boolean groupSpy;
    private boolean canBeMessaged;
    private boolean tagSounds;
    private boolean messageSounds;
    private boolean emojis;
    private long muteTime;
    private ChatChannel currentChannel;
    private String color;
    private List<GroupChat> groupInvites;

    /**
     * Creates a new PlayerData for a specific player.
     * @param uuid The UUID of the player.
     */
    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.messageLog = new MessageLog(uuid);
        this.canBeMessaged = true;
        this.tagSounds = true;
        this.messageSounds = true;
        this.emojis = true;
        this.color = "&f";
        this.currentChannel = RoseChatAPI.getInstance().getChannelManager().getDefaultChannel();
        this.groupInvites = new ArrayList<>();
    }

    /**
     * Saves player data to the database.
     */
    public void save() {
        RoseChat.getInstance().getManager(DataManager.class).updatePlayerData(this);
    }

    /**
     * Gets the UUID of the player who owns this player data.
     * @return The UUID.
     */
    public UUID getUuid() {
        return this.uuid;
    }

    /**
     * Gets the message log.
     * @return The message log.
     */
    public MessageLog getMessageLog() {
        return this.messageLog;
    }

    /**
     * Gets who the player is replying to.
     * @return The player to reply to.
     */
    public String getReplyTo() {
        return this.replyTo;
    }

    /**
     * Sets who the player is replying to.
     * @param replyTo The player to reply to.
     */
    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    /**
     * Whether or not the player has message spy enabled.
     * @return True if the player has message spy enabled.
     */
    public boolean hasMessageSpy() {
        return this.messageSpy;
    }

    /**
     * Sets whether or not the player has message spy enabled.
     * @param messageSpy Whether or not the player has message spy enabled.
     */
    public void setMessageSpy(boolean messageSpy) {
        this.messageSpy = messageSpy;
    }

    /**
     * Whether or not the player has channel spy enabled.
     * @return True if the player has channel spy enabled.
     */
    public boolean hasChannelSpy() {
        return this.channelSpy;
    }

    /**
     * Sets whether or not the player has channel spy enabled.
     * @param channelSpy Whether or not the player has channel spy enabled.
     */
    public void setChannelSpy(boolean channelSpy) {
        this.channelSpy = channelSpy;
    }

    /**
     * Whether or not the player has channel spy enabled.
     * @return True if the player has channel spy enabled.
     */
    public boolean hasGroupSpy() {
        return this.groupSpy;
    }

    /**
     * Sets whether or not the player has group spy enabled.
     * @param groupSpy Whether or not the player has group spy enabled.
     */
    public void setGroupSpy(boolean groupSpy) {
        this.groupSpy = groupSpy;
    }

    /**
     * Whether or not the player can be messaged.
     * @return True if the player can be messaged.
     */
    public boolean canBeMessaged() {
        return this.canBeMessaged;
    }

    /**
     * Sets whether or not the player can be messaged.
     * @param canBeMessaged Whether or not the player can be messaged.
     */
    public void setCanBeMessaged(boolean canBeMessaged) {
        this.canBeMessaged = canBeMessaged;
    }

    /**
     * Whether or not the player has tag sounds enabled.
     * @return True if the player has tag sounds enabled.
     */
    public boolean hasTagSounds() {
        return this.tagSounds;
    }

    /**
     * Sets whether or not the player has tag sounds enabled.
     * @param tagSounds Whether or not the player has tag sounds enabled.
     */
    public void setTagSounds(boolean tagSounds) {
        this.tagSounds = tagSounds;
    }

    /**
     * Whether or not the player has message sounds enabled.
     * @return True if the player has message sounds enabled.
     */
    public boolean hasMessageSounds() {
        return this.messageSounds;
    }

    /**
     * Sets whether or not the player has message sounds enabled.
     * @param messageSounds Whether or not the player has message sounds enabled.
     */
    public void setMessageSounds(boolean messageSounds) {
        this.messageSounds = messageSounds;
    }

    /**
     * Whether or not the player has emojis enabled.
     * @return True if the player has emojis enabled.
     */
    public boolean hasEmojis() {
        return this.emojis;
    }

    /**
     * Sets whether or not the player has emojis enabled.
     * @param emojis Whether or not the player has emojis enabled.
     */
    public void setEmojis(boolean emojis) {
        this.emojis = emojis;
    }

    /**
     * Gets the current channel the player is in.
     * @return The current channel the player is in.
     */
    public ChatChannel getCurrentChannel() {
        return this.currentChannel;
    }

    /**
     * Sets the current channel that the player is in.
     * @param currentChannel The channel to use.
     */
    public void setCurrentChannel(ChatChannel currentChannel) {
        this.currentChannel = currentChannel;
    }

    /**
     * Gets the amount of time left in a player's mute, in seconds.
     * @return The amount of time left in a player's mute, in seconds.
     */
    public long getMuteTime() {
        return this.muteTime;
    }

    /**
     * Sets the amount of time a player is muted, in seconds.
     * @param muteTime The time to mute the player, in seconds.
     */
    public void setMuteTime(long muteTime) {
        this.muteTime = muteTime;
    }

    /**
     * Gets the current chat color of the player.
     * @return The current chat color of the player.
     */
    public String getColor() {
        return this.color;
    }

    /**
     * Sets the current chat color of the player.
     * @param color The color to use.
     */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     * @return The invites the player has received.
     */
    public List<GroupChat> getGroupInvites() {
        return this.groupInvites;
    }

    /**
     * Adds to the group invites list.
     * @param groupChat The group chat to add.
     */
    public void inviteToGroup(GroupChat groupChat) {
        this.groupInvites.add(groupChat);
    }
}

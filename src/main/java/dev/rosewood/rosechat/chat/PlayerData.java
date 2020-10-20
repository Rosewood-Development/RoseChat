package dev.rosewood.rosechat.chat;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.managers.DataManager;

import java.util.UUID;

public class PlayerData {

    private UUID uuid;
    private MessageLog messageLog;
    private UUID replyTo;
    private boolean socialSpy;
    private boolean canBeMessaged;
    private boolean tagSounds;
    private boolean messageSounds;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.messageLog = new MessageLog(uuid);
        this.canBeMessaged = true;
    }

    public PlayerData(UUID uuid, boolean socialSpy, boolean canBeMessaged, boolean tagSounds, boolean messageSounds) {
        this(uuid);
        this.socialSpy = socialSpy;
        this.canBeMessaged = canBeMessaged;
        this.tagSounds = tagSounds;
        this.messageSounds = messageSounds;
    }

    public void save() {
        RoseChat.getInstance().getManager(DataManager.class).updatePlayerData(this);
    }

    public UUID getUuid() {
        return uuid;
    }

    public MessageLog getMessageLog() {
        return messageLog;
    }

    public void setReplyTo(UUID replyTo) {
        this.replyTo = replyTo;
    }

    public UUID getReplyTo() {
        return replyTo;
    }

    public void setSocialSpy(boolean socialSpy) {
        this.socialSpy = socialSpy;
    }

    public boolean hasSocialSpy() {
        return socialSpy;
    }

    public void setCanBeMessaged(boolean canBeMessaged) {
        this.canBeMessaged = canBeMessaged;
    }

    public boolean canBeMessaged() {
        return canBeMessaged;
    }

    public void setTagSounds(boolean tagSounds) {
        this.tagSounds = tagSounds;
    }

    public boolean hasTagSounds() {
        return tagSounds;
    }

    public void setMessageSounds(boolean messageSounds) {
        this.messageSounds = messageSounds;
    }

    public boolean hasMessageSounds() {
        return messageSounds;
    }
}

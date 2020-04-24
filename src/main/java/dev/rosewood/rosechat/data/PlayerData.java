package dev.rosewood.rosechat.data;

import dev.rosewood.rosechat.channels.ChatChannel;
import dev.rosewood.rosechat.groups.Group;

import java.util.List;
import java.util.UUID;

public class PlayerData {

    private UUID uuid;
    private UUID lastRecipient;

    private boolean isStaffChatOpen;
    private boolean isSoundOn;
    private boolean canBeMessaged;
    private boolean canBeTagged;

    private ChatChannel openChannel;
    private List<ChatChannel> spyingChannels;
    private List<Group> groupChats;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    public UUID getLastRecipient() {
        return lastRecipient;
    }

    public void setLastRecipient(UUID lastRecipient) {
        this.lastRecipient = lastRecipient;
    }

    public boolean isStaffChatOpen() {
        return isStaffChatOpen;
    }

    public void setStaffChatOpen(boolean staffChatOpen) {
        isStaffChatOpen = staffChatOpen;
    }

    public boolean toggleStaffChat() {
        isStaffChatOpen = !isStaffChatOpen;
        return isStaffChatOpen;
    }

    public boolean isSoundOn() {
        return isSoundOn;
    }

    public void setSoundOn(boolean soundOn) {
        isSoundOn = soundOn;
    }

    public boolean toggleSound() {
        isSoundOn = !isSoundOn;
        return isSoundOn;
    }

    public boolean canBeMessaged() {
        return canBeMessaged;
    }

    public void setCanBeMessaged(boolean canBeMessaged) {
        this.canBeMessaged = canBeMessaged;
    }

    public boolean toggleMessages() {
        canBeMessaged = !canBeMessaged;
        return canBeMessaged;
    }

    public boolean canBeTagged() {
        return canBeTagged;
    }

    public void setCanBeTagged(boolean canBeTagged) {
        this.canBeTagged = canBeTagged;
    }

    public boolean toggleTagging() {
        canBeTagged = !canBeTagged;
        return canBeTagged;
    }

    public ChatChannel getOpenChannel() {
        return openChannel;
    }

    public void setOpenChannel(ChatChannel openChannel) {
        this.openChannel = openChannel;
    }

    public List<ChatChannel> getSpyingChannels() {
        return spyingChannels;
    }

    public void setSpyingChannels(List<ChatChannel> spyingChannels) {
        this.spyingChannels = spyingChannels;
    }

    public boolean isSpying() {
        return spyingChannels.size() > 0;
    }

    public boolean isSpying(ChatChannel channelType) {
        return spyingChannels.contains(channelType);
    }

    public void startSpying(ChatChannel channel) {
        this.spyingChannels.add(channel);
    }

    public void stopSpying(ChatChannel chatChannel) {
        this.spyingChannels.remove(chatChannel);
    }

    public List<Group> getGroupChats() {
        return groupChats;
    }

    public void setActiveGroupChats(List<Group> groupChats) {
        this.groupChats = groupChats;
    }

    public void joinGroup(Group group) {
        groupChats.add(group);
    }

    public void leaveGroup(Group group) {
        groupChats.remove(group);
    }
}

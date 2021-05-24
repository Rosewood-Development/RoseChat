package dev.rosewood.rosechat.manager;

import dev.rosewood.rosechat.chat.GroupChat;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.manager.Manager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GroupManager extends Manager {

    private Map<String, GroupChat> groupChats;

    public GroupManager(RosePlugin rosePlugin) {
        super(rosePlugin);
        this.groupChats = new HashMap<>();
    }

    @Override
    public void reload() {

    }

    @Override
    public void disable() {

    }

    public void addGroupChat(GroupChat groupChat) {
        this.groupChats.put(groupChat.getUuid().toString(), groupChat);
    }

    public void removeGroupChat(GroupChat groupChat) {
        this.groupChats.remove(groupChat.getUuid().toString());
    }

    public Map<String, GroupChat> getGroupChats() {
        return this.groupChats;
    }

    public GroupChat getGroupChatByOwner(UUID owner) {
        for (GroupChat groupChat : this.groupChats.values()) {
            if (groupChat.getOwner().equals(owner)) {
                return groupChat;
            }
        }

        return null;
    }
}

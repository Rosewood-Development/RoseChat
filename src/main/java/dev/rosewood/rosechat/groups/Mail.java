package dev.rosewood.rosechat.groups;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Mail {

    private Group group;
    private UUID sender;
    private List<UUID> unreadPlayers;
    private String message;

    public Mail(Group group, UUID sender, String message) {
        this.group = group;
        this.sender = sender;
        this.message = message;
        this.unreadPlayers = new ArrayList<>();
    }

    public boolean hasRead(UUID uuid) {
        return !unreadPlayers.contains(uuid);
    }

    public void setRead(UUID uuid) {
        unreadPlayers.remove(uuid);

        if (unreadPlayers.size() == 0) group.getMail().remove(this);
    }

    public Group getGroup() {
        return group;
    }

    public UUID getSender() {
        return sender;
    }

    public List<UUID> getUnreadPlayers() {
        return unreadPlayers;
    }

    public String getMessage() {
        return message;
    }

    public void setUnreadPlayers(List<UUID> unreadPlayers) {
        this.unreadPlayers = unreadPlayers;
    }
}

package dev.rosewood.rosechat.api.event.group;

import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;
import org.bukkit.event.HandlerList;

public class GroupNameChangedEvent extends GroupEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final String newName;

    /**
     * Called when a group name is changed.
     * @param group The {@link GroupChannel} for the group whose name was changed.
     * @param newName The new name for the group.
     */
    public GroupNameChangedEvent(GroupChannel group, String newName) {
        super(group);
        this.newName = newName;
    }

    public String getNewName() {
        return this.newName;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}

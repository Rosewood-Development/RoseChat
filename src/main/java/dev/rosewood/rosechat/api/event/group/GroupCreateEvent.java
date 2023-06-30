package dev.rosewood.rosechat.api.event.group;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class GroupCreateEvent extends GroupEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Called when a group is created.
     * @param id The ID of the group.
     * @param owner The owner of the group.
     */
    public GroupCreateEvent(String id, Player owner) {
        super(null);
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}

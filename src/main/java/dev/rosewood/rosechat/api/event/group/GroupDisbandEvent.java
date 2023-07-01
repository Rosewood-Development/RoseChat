package dev.rosewood.rosechat.api.event.group;

import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;
import org.bukkit.event.HandlerList;

public class GroupDisbandEvent extends GroupEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Called when a group is disbanded.
     * @param group The {@link GroupChannel} for the group that was disbanded.
     */
    public GroupDisbandEvent(GroupChannel group) {
        super(group);
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}

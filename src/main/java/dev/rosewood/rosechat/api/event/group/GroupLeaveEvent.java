package dev.rosewood.rosechat.api.event.group;

import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;
import org.bukkit.event.HandlerList;
import java.util.UUID;

public class GroupLeaveEvent extends GroupEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final UUID memberUUID;

    /**
     * Called when a player leaves a group.
     * @param group The {@link GroupChannel} for the group that was left.
     * @param memberUUID The {@link UUID} for the player that left the group.
     */
    public GroupLeaveEvent(GroupChannel group, UUID memberUUID) {
        super(group);
        this.memberUUID = memberUUID;
    }

    public UUID getMemberUUID() {
        return this.memberUUID;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}

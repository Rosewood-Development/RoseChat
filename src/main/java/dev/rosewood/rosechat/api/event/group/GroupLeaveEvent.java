package dev.rosewood.rosechat.api.event.group;

import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;
import java.util.UUID;

public class GroupLeaveEvent extends GroupEvent {

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

    /**
     * @return The {@link UUID} for the player that left the group.
     */
    public UUID getMemberUUID() {
        return this.memberUUID;
    }

}

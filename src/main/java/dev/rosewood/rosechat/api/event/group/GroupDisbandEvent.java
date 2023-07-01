package dev.rosewood.rosechat.api.event.group;

import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;

public class GroupDisbandEvent extends GroupEvent {

    /**
     * Called when a group is disbanded.
     * @param group The {@link GroupChannel} for the group that was disbanded.
     */
    public GroupDisbandEvent(GroupChannel group) {
        super(group);
    }

}

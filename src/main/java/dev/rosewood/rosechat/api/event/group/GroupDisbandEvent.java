package dev.rosewood.rosechat.api.event.group;

import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;

public class GroupDisbandEvent extends GroupEvent {

    /**
     * Called when a {@link GroupChannel} is about to be disbanded.
     * @param group The {@link GroupChannel} that will be disbanded.
     */
    public GroupDisbandEvent(GroupChannel group) {
        super(group);
    }

}

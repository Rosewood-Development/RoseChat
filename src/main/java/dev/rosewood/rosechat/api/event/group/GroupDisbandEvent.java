package dev.rosewood.rosechat.api.event.group;

import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;

public class GroupDisbandEvent extends GroupEvent {

    public GroupDisbandEvent(GroupChannel group) {
        super(group);
    }

}

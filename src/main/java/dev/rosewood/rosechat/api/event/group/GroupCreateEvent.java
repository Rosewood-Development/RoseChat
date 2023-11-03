package dev.rosewood.rosechat.api.event.group;

import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;

public class GroupCreateEvent extends GroupEvent {

    public GroupCreateEvent(GroupChannel group) {
        super(group);
    }

}

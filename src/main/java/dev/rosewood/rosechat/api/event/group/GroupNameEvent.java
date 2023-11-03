package dev.rosewood.rosechat.api.event.group;

import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;

public class GroupNameEvent extends GroupEvent {

    private String name;

    public GroupNameEvent(GroupChannel group, String name) {
        super(group);
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

}

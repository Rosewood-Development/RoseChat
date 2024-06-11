package dev.rosewood.rosechat.api.event.group;

import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;

public class GroupCreateEvent extends GroupEvent {

    /**
     * Called when a new {@link GroupChannel} is created.
     * Use {@link GroupPreCreateEvent} for a cancellable event.
     * @param group The new {@link GroupChannel} that was created.
     */
    public GroupCreateEvent(GroupChannel group) {
        super(group);
    }

}

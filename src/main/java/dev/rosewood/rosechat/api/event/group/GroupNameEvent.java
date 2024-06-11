package dev.rosewood.rosechat.api.event.group;

import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;

public class GroupNameEvent extends GroupEvent {

    private String name;

    /**
     * Called when a {@link GroupChannel} is about to be renamed.
     * @param group The {@link GroupChannel} that will be renamed.
     * @param name The new name of the {@link GroupChannel}.
     */
    public GroupNameEvent(GroupChannel group, String name) {
        super(group);

        this.name = name;
    }

    /**
     * @return The new name of the {@link GroupChannel}.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Updates the name of the {@link GroupChannel}.
     * @param name The new name to use.
     */
    public void setName(String name) {
        this.name = name;
    }

}

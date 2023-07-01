package dev.rosewood.rosechat.api.event.group;

import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;

public class GroupNameChangedEvent extends GroupEvent {

    private String newName;

    /**
     * Called when a group name is changed.
     * @param group The {@link GroupChannel} for the group whose name was changed.
     * @param newName The new name for the group.
     */
    public GroupNameChangedEvent(GroupChannel group, String newName) {
        super(group);
        this.newName = newName;
    }

    /**
     * @return The new name for the group.
     */
    public String getNewName() {
        return this.newName;
    }

    /**
     * Sets the new name for the group.
     * @param newName The new name for the group.
     */
    public void setNewName(String newName) {
        this.newName = newName;
    }

}

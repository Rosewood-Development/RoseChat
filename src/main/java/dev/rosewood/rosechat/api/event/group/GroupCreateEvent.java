package dev.rosewood.rosechat.api.event.group;

import org.bukkit.entity.Player;

public class GroupCreateEvent extends GroupEvent {

    private final String id;
    private final Player owner;

    /**
     * Called when a group is created.
     * @param id The ID of the group.
     * @param owner The owner of the group.
     */
    public GroupCreateEvent(String id, Player owner) {
        super(null);
        this.id = id;
        this.owner = owner;
    }

    /**
     * @return The ID of the group.
     */
    public String getId() {
        return this.id;
    }

    /**
     * @return The owner of the group.
     */
    public Player getOwner() {
        return this.owner;
    }

}

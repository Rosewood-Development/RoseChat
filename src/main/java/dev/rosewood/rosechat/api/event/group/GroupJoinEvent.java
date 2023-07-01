package dev.rosewood.rosechat.api.event.group;

import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;
import org.bukkit.entity.Player;

public class GroupJoinEvent extends GroupEvent {

    private final Player player;

    /**
     * Called when a player joins a group.
     * @param group The {@link GroupChannel} for the group that was joined.
     * @param player The {@link Player} for the player that joined the group.
     */
    public GroupJoinEvent(GroupChannel group, Player player) {
        super(group);
        this.player = player;
    }

    /**
     * @return The {@link Player} for the player that joined the group.
     */
    public Player getPlayer() {
        return this.player;
    }

}

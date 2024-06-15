package dev.rosewood.rosechat.api.event.group;

import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;
import org.bukkit.entity.Player;

public class GroupJoinEvent extends GroupEvent {

    private final Player player;

    /**
     * Called when a player joins a {@link GroupChannel}.
     * @param group The {@link GroupChannel} that will be joined.
     * @param player The {@link Player} joining the group.
     */
    public GroupJoinEvent(GroupChannel group, Player player) {
        super(group);

        this.player = player;
    }

    /**
     * @return The {@link Player} joining the group.
     */
    public Player getPlayer() {
        return this.player;
    }

}

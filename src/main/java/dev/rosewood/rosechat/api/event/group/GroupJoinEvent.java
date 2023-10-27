package dev.rosewood.rosechat.api.event.group;

import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;
import org.bukkit.entity.Player;

public class GroupJoinEvent extends GroupEvent {

    private final Player player;

    public GroupJoinEvent(GroupChannel group, Player player) {
        super(group);
        this.player = player;
    }

    public Player getPlayer() {
        return this.player;
    }

}

package dev.rosewood.rosechat.api.event.group;

import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;
import org.bukkit.OfflinePlayer;

public class GroupLeaveEvent extends GroupEvent {

    private final OfflinePlayer player;

    public GroupLeaveEvent(GroupChannel group, OfflinePlayer player) {
        super(group);
        this.player = player;
    }

    public OfflinePlayer getPlayer() {
        return this.player;
    }

}

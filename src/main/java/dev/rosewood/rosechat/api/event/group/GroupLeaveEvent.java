package dev.rosewood.rosechat.api.event.group;

import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;
import org.bukkit.OfflinePlayer;

public class GroupLeaveEvent extends GroupEvent {

    private final OfflinePlayer player;
    private final boolean isKicked;

    public GroupLeaveEvent(GroupChannel group, OfflinePlayer player, boolean isKicked) {
        super(group);
        this.player = player;
        this.isKicked = isKicked;
    }

    public OfflinePlayer getPlayer() {
        return this.player;
    }

    public boolean isKicked() {
        return this.isKicked;
    }

}

package dev.rosewood.rosechat.api.event.group;

import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;
import org.bukkit.OfflinePlayer;

public class GroupLeaveEvent extends GroupEvent {

    private final OfflinePlayer player;
    private final boolean isKicked;

    /**
     * Called when an {@link OfflinePlayer} leaves a {@link GroupChannel}.
     * @param group The {@link GroupChannel} that the {@link OfflinePlayer} is leaving.
     * @param player The {@link OfflinePlayer} that is leaving the {@link GroupChannel}.
     * @param isKicked True if the player was manually kicked from the group by someone else.
     */
    public GroupLeaveEvent(GroupChannel group, OfflinePlayer player, boolean isKicked) {
        super(group);

        this.player = player;
        this.isKicked = isKicked;
    }

    /**
     * @return The {@link OfflinePlayer} that is leaving the {@link GroupChannel}.
     */
    public OfflinePlayer getPlayer() {
        return this.player;
    }

    /**
     * @return True if the player was manually kicked from the group by someone else.
     */
    public boolean isKicked() {
        return this.isKicked;
    }

}

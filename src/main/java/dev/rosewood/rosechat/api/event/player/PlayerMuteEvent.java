package dev.rosewood.rosechat.api.event.player;

import dev.rosewood.rosechat.message.RosePlayer;

public class PlayerMuteEvent extends RosePlayerEvent {

    private int muteTime;

    /**
     * Called when a player is muted.
     * @param player The {@link RosePlayer} for the player who is being muted.
     * @param muteTime The time the player is being muted for.
     */
    public PlayerMuteEvent(RosePlayer player, int muteTime) {
        super(player);
        this.muteTime = muteTime;
    }

    /**
     * @return The time the player is being muted for, in seconds.
     */
    public int getMuteTime() {
        return this.muteTime;
    }

    /**
     * Sets the time the player is being muted for, in seconds.
     * @param muteTime The time the player is being muted for.
     */
    public void setMuteTime(int muteTime) {
        this.muteTime = muteTime;
    }

}

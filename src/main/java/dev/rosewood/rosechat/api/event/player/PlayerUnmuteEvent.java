package dev.rosewood.rosechat.api.event.player;

import dev.rosewood.rosechat.message.RosePlayer;

public class PlayerUnmuteEvent extends RosePlayerEvent {

    /**
     * Called when a player is unmuted.
     * @param player The {@link RosePlayer} for the player who is being unmuted.
     */
    public PlayerUnmuteEvent(RosePlayer player) {
        super(player);
    }

}

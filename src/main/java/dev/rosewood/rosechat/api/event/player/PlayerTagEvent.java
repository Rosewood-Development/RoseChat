package dev.rosewood.rosechat.api.event.player;

import dev.rosewood.rosechat.message.RosePlayer;

public class PlayerTagEvent extends RosePlayerEvent {

    private final RosePlayer sender;

    /**
     * Called when a player is muted.
     * @param player The {@link RosePlayer} for the player who is being muted.
     */
    public PlayerTagEvent(RosePlayer player, RosePlayer sender) {
        super(player);
        this.sender = sender;
    }

    /**
     * @return The {@link RosePlayer} for the player who is tagging the player.
     */
    public RosePlayer getSender() {
        return this.sender;
    }

}

package dev.rosewood.rosechat.api.event.player;

import dev.rosewood.rosechat.message.RosePlayer;
import org.bukkit.event.HandlerList;

public class PlayerUnmuteEvent extends RosePlayerEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Called when a player is unmuted.
     * @param player The {@link RosePlayer} for the player who is being unmuted.
     */
    public PlayerUnmuteEvent(RosePlayer player) {
        super(player);
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}

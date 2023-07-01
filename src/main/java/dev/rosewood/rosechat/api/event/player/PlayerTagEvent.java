package dev.rosewood.rosechat.api.event.player;

import dev.rosewood.rosechat.message.RosePlayer;
import org.bukkit.event.HandlerList;

public class PlayerTagEvent extends RosePlayerEvent {

    private static final HandlerList HANDLERS = new HandlerList();

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

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}

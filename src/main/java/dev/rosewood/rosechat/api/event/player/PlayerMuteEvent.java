package dev.rosewood.rosechat.api.event.player;

import dev.rosewood.rosechat.message.RosePlayer;
import org.bukkit.event.HandlerList;

public class PlayerMuteEvent extends RosePlayerEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final int muteTime;

    /**
     * Called when a player is muted.
     * @param player The {@link RosePlayer} for the player who is being muted.
     * @param muteTime The time the player is being muted for.
     */
    public PlayerMuteEvent(RosePlayer player, int muteTime) {
        super(player);
        this.muteTime = muteTime;
    }

    public int getMuteTime() {
        return this.muteTime;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}

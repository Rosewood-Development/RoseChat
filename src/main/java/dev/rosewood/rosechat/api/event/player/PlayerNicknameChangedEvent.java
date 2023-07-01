package dev.rosewood.rosechat.api.event.player;

import dev.rosewood.rosechat.message.RosePlayer;
import org.bukkit.event.HandlerList;

public class PlayerNicknameChangedEvent extends RosePlayerEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private String newNickname;

    /**
     * Called when a player's nickname is changed.
     * @param player The {@link RosePlayer} for the player whose nickname is being changed.
     */
    public PlayerNicknameChangedEvent(RosePlayer player, String newNickname) {
        super(player);
        this.newNickname = newNickname;
    }

    public String getNewNickname() {
        return this.newNickname;
    }

    public void setNewNickname(String newNickname) {
        this.newNickname = newNickname;
    }

    public String getNickname() {
        return this.getPlayer().getNickname();
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}

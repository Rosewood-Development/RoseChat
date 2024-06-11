package dev.rosewood.rosechat.api.event.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerNicknameEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private String nickname;
    private boolean cancelled;

    /**
     * Called when a player's nickname is updated.
     * @param player The
     * @param nickname
     */
    public PlayerNicknameEvent(Player player, String nickname) {
        super(player);

        this.nickname = nickname;
    }

    /**
     * @return The nickname that will be used.
     */
    public String getNickname() {
        return this.nickname;
    }

    /**
     * Updates the nickname that will be used.
     * @param nickname The new nickname to use.
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

}

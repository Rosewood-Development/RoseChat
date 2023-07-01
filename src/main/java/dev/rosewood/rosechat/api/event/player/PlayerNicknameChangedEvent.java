package dev.rosewood.rosechat.api.event.player;

import dev.rosewood.rosechat.message.RosePlayer;

public class PlayerNicknameChangedEvent extends RosePlayerEvent {

    private String newNickname;

    /**
     * Called when a player's nickname is changed.
     * @param player The {@link RosePlayer} for the player whose nickname is being changed.
     */
    public PlayerNicknameChangedEvent(RosePlayer player, String newNickname) {
        super(player);
        this.newNickname = newNickname;
    }

    /**
     * @return The new nickname of the player.
     */
    public String getNewNickname() {
        return this.newNickname;
    }

    /**
     * Sets the new nickname of the player.
     * @param newNickname The new nickname of the player.
     */
    public void setNewNickname(String newNickname) {
        this.newNickname = newNickname;
    }

    /**
     * @return The old nickname of the player.
     */
    public String getNickname() {
        return this.getPlayer().getNickname();
    }

}

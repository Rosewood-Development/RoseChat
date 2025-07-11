package dev.rosewood.rosechat.api.event.player;

import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.RoseMessage;

public class PlayerSendMessageEvent extends PlayerMessageEvent {

    /**
     * Called when a player sends a private message.
     * @param sender The {@link RosePlayer} who is sending the message.
     * @param receiver The {@link RosePlayer} who is receiving the message.
     * @param message The {@link RoseMessage} that will be sent.
     */
    public PlayerSendMessageEvent(RosePlayer sender, RosePlayer receiver, RoseMessage message) {
        super(sender, receiver, message);
    }

}

package dev.rosewood.rosechat.api.event.player;

import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;

public class PlayerSendMessageEvent extends PlayerMessageEvent {

    public PlayerSendMessageEvent(RosePlayer sender, RosePlayer receiver, RoseMessage message) {
        super(sender, receiver, message);
    }

}

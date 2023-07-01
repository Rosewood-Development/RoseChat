package dev.rosewood.rosechat.api.event.message;

import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosechat.message.RosePlayer;

public class PreParseMessageEvent extends MessageEvent {

    /**
     * Called when a message is about to be parsed. Useful for editing the message.
     * @param message The {@link RoseMessage} for the message that will be parsed.
     * @param viewer The {@link RosePlayer} for the person viewing the message.
     */
    public PreParseMessageEvent(RoseMessage message, RosePlayer viewer) {
        super(message, viewer);
    }

}

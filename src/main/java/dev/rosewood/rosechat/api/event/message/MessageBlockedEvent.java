package dev.rosewood.rosechat.api.event.message;

import dev.rosewood.rosechat.message.wrapper.RoseMessage;

public class MessageBlockedEvent extends MessageEvent {

    /**
     * Called when a message is blocked.
     * @param message The {@link RoseMessage} for the message that will be parsed.
     */
    public MessageBlockedEvent(RoseMessage message) {
        super(message, null);
    }

}

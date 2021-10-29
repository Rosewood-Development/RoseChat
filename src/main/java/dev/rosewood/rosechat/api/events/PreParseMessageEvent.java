package dev.rosewood.rosechat.api.events;

import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;

public class PreParseMessageEvent extends MessageEvent {

    /**
     * Called when a message is about to be parsed. Useful for editing the message.
     * @param message The {@link MessageWrapper} for the message that will be parsed.
     * @param viewer The {@link RoseSender} for the person viewing the message.
     */
    public PreParseMessageEvent(MessageWrapper message, RoseSender viewer) {
        super(message, viewer);
    }
}

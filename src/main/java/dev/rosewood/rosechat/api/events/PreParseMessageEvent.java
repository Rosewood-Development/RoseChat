package dev.rosewood.rosechat.api.events;

import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.MessageWrapper;

public class PreParseMessageEvent extends MessageEvent {

    /**
     * Called when a message is about to be parsed. Useful for editing the message.
     * @param message The MessageWrapper for the message that will be parsed.
     * @param viewer The RoseSender for the person viewing the message.
     */
    public PreParseMessageEvent(MessageWrapper message, RoseSender viewer) {
        super(message, viewer);
    }
}

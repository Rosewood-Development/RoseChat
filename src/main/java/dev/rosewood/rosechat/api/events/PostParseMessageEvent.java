package dev.rosewood.rosechat.api.events;

import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.MessageWrapper;

public class PostParseMessageEvent extends MessageEvent {

    /**
     * Called after a message has been parsed.
     * @param message The MessageWrapper for the message that will be parsed.
     * @param viewer The RoseSender for the person viewing the message.
     */
    public PostParseMessageEvent(MessageWrapper message, RoseSender viewer) {
        super(message, viewer);
    }
}

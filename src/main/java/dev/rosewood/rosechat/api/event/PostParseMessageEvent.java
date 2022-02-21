package dev.rosewood.rosechat.api.event;

import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;

public class PostParseMessageEvent extends MessageEvent {

    /**
     * Called after a message has been parsed.
     * @param message The {@link MessageWrapper} for the message that will be parsed.
     * @param viewer The {@link RoseSender} for the person viewing the message.
     */
    public PostParseMessageEvent(MessageWrapper message, RoseSender viewer) {
        super(message, viewer);
    }
}

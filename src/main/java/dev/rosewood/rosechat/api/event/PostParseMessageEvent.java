package dev.rosewood.rosechat.api.event;

import dev.rosewood.rosechat.message.MessageDirection;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.wrapper.RoseMessageComponents;

public class PostParseMessageEvent extends MessageEvent {

    private final MessageDirection messageDirection;
    private RoseMessageComponents messageComponents;

    /**
     * Called after a message has been parsed.
     * @param message The {@link RoseMessage} for the message that will be parsed.
     * @param viewer The {@link RosePlayer} for the person viewing the message.
     * @param messageDirection Where this message is going.
     * @param messageComponents The {@link RoseMessageComponents} for the parsed message.
     */
    public PostParseMessageEvent(RoseMessage message, RosePlayer viewer, MessageDirection messageDirection, RoseMessageComponents messageComponents) {
        super(message, viewer);
        this.messageDirection = messageDirection;
        this.messageComponents = messageComponents;
    }

    public MessageDirection getMessageDirection() {
        return this.messageDirection;
    }

    /**
     * @return The {@link RoseMessageComponents} for the parsed message.
     */
    public RoseMessageComponents getMessageComponents() {
        return this.messageComponents;
    }

    /**
     * Sets the {@link RoseMessageComponents} for the parsed message.
     * @param messageComponents The {@link RoseMessageComponents} for the parsed message.
     */
    public void setMessageComponents(RoseMessageComponents messageComponents) {
        this.messageComponents = messageComponents;
    }

}

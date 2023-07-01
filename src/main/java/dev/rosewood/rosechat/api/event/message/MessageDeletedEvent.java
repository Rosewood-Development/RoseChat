package dev.rosewood.rosechat.api.event.message;

import dev.rosewood.rosechat.message.DeletableMessage;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;

public class MessageDeletedEvent extends DeleteMessageEvent {

    /**
     * Called after a message has been deleted.
     * @param message The {@link RoseMessage} for the message that was deleted.
     * @param deleter The {@link RosePlayer} for the person who deleted the message.
     */
    public MessageDeletedEvent(DeletableMessage message, RosePlayer deleter) {
        super(message, deleter);
    }

}

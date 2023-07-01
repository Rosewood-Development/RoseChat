package dev.rosewood.rosechat.api.event.message;

import dev.rosewood.rosechat.message.DeletableMessage;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;

public class MessageDeleteEvent extends DeleteMessageEvent {

    /**
     * Called before a message has been deleted.
     * @param message The {@link RoseMessage} for the message that will be deleted.
     * @param deleter The {@link RosePlayer} for the person deleting the message.
     */
    public MessageDeleteEvent(DeletableMessage message, RosePlayer deleter) {
        super(message, deleter);
    }

}

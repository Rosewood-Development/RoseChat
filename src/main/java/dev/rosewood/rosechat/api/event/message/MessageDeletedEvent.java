package dev.rosewood.rosechat.api.event.message;

import dev.rosewood.rosechat.message.DeletableMessage;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import org.bukkit.event.HandlerList;

public class MessageDeletedEvent extends DeleteMessageEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Called after a message has been deleted.
     * @param message The {@link RoseMessage} for the message that was deleted.
     * @param deleter The {@link RosePlayer} for the person who deleted the message.
     */
    public MessageDeletedEvent(DeletableMessage message, RosePlayer deleter) {
        super(message, deleter);
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}

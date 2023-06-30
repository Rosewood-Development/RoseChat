package dev.rosewood.rosechat.api.event.message;

import dev.rosewood.rosechat.message.MessageDirection;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosechat.message.RosePlayer;
import org.bukkit.event.HandlerList;

public class PostParseMessageEvent extends MessageEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private MessageDirection messageDirection;

    /**
     * Called after a message has been parsed.
     * @param message The {@link RoseMessage} for the message that will be parsed.
     * @param viewer The {@link RosePlayer} for the person viewing the message.
     */
    public PostParseMessageEvent(RoseMessage message, RosePlayer viewer) {
        super(message, viewer);
    }

    /**
     * Called after a message has been parsed.
     * @param message The {@link RoseMessage} for the message that will be parsed.
     * @param viewer The {@link RosePlayer} for the person viewing the message.
     * @param messageDirection Where this message is going.
     */
    public PostParseMessageEvent(RoseMessage message, RosePlayer viewer, MessageDirection messageDirection) {
        this(message, viewer);
        this.messageDirection = messageDirection;
    }

    public MessageDirection getMessageDirection() {
        return this.messageDirection;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}

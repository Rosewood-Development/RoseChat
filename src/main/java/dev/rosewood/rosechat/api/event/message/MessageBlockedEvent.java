package dev.rosewood.rosechat.api.event.message;

import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import org.bukkit.event.HandlerList;

public class MessageBlockedEvent extends MessageEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Called when a message is blocked.
     * @param message The {@link RoseMessage} for the message that will be parsed.
     */
    public MessageBlockedEvent(RoseMessage message) {
        super(message, null);
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}

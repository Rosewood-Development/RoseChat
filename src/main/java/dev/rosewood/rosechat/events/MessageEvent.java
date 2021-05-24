package dev.rosewood.rosechat.events;

import dev.rosewood.rosechat.message.MessageWrapper;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MessageEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final MessageWrapper message;
    private boolean cancelled;

    public MessageEvent(MessageWrapper message) {
        this.message = message;
    }

    public MessageWrapper getMessage() {
        return this.message;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}

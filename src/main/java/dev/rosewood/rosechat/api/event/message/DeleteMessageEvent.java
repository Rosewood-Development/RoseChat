package dev.rosewood.rosechat.api.event.message;

import dev.rosewood.rosechat.message.DeletableMessage;
import dev.rosewood.rosechat.message.RosePlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public abstract class DeleteMessageEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final DeletableMessage message;
    private final RosePlayer deleter;
    private boolean cancelled;

    public DeleteMessageEvent(DeletableMessage message, RosePlayer deleter) {
        this.message = message;
        this.deleter = deleter;
    }

    public DeletableMessage getMessage() {
        return this.message;
    }

    public RosePlayer getDeleter() {
        return this.deleter;
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

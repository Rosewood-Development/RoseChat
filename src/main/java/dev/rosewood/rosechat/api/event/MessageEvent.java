package dev.rosewood.rosechat.api.event;

import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosechat.message.RosePlayer;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MessageEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final RoseMessage message;
    private final RosePlayer viewer;
    private boolean cancelled;

    public MessageEvent(RoseMessage message, RosePlayer viewer) {
        super(!Bukkit.isPrimaryThread());
        this.message = message;
        this.viewer = viewer;
    }

    public RoseMessage getMessage() {
        return this.message;
    }

    public RosePlayer getViewer() {
        return this.viewer;
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

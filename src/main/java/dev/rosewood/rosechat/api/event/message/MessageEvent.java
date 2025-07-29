package dev.rosewood.rosechat.api.event.message;

import dev.rosewood.rosechat.message.MessageDirection;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.RoseMessage;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@SuppressWarnings("unused")
public class MessageEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final MessageDirection direction;
    private final RoseMessage message;
    private final RosePlayer viewer;
    private boolean cancelled;

    public MessageEvent(RoseMessage message, RosePlayer viewer, MessageDirection direction) {
        super(!Bukkit.isPrimaryThread());

        this.message = message;
        this.viewer = viewer;
        this.direction = direction;
    }

    /**
     * @return The {@link RoseMessage} that was sent.
     */
    public RoseMessage getMessage() {
        return this.message;
    }

    /**
     * @return The {@link RosePlayer} viewing the message.
     *         This is not always the player receiving the message.
     */
    public RosePlayer getViewer() {
        return this.viewer;
    }

    /**
     * @return The {@link MessageDirection} for where this message should be going.
     */
    public MessageDirection getDirection() {
        return this.direction;
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

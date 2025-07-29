package dev.rosewood.rosechat.api.event.player;

import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.RoseMessage;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@SuppressWarnings("unused")
public class PlayerMessageEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final RosePlayer sender;
    private final RosePlayer receiver;
    private final RoseMessage message;
    private boolean cancelled;

    public PlayerMessageEvent(RosePlayer sender, RosePlayer receiver, RoseMessage message) {
        super(!Bukkit.isPrimaryThread());

        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
    }

    /**
     * @return The {@link RosePlayer} sending the message.
     */
    public RosePlayer getSender() {
        return this.sender;
    }

    /**
     * @return The {@link RosePlayer} receiving the message.
     */
    public RosePlayer getReceiver() {
        return this.receiver;
    }

    /**
     * @return The {@link RoseMessage} that was sent.
     */
    public RoseMessage getMessage() {
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

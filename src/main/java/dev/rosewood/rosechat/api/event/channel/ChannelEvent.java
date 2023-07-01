package dev.rosewood.rosechat.api.event.channel;

import dev.rosewood.rosechat.chat.channel.Channel;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public abstract class ChannelEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Channel channel;
    private boolean cancelled;

    public ChannelEvent(Channel channel) {
        super(!Bukkit.isPrimaryThread());
        this.channel = channel;
    }

    /**
     * @return The {@link Channel} for this event.
     */
    public Channel getChannel() {
        return this.channel;
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
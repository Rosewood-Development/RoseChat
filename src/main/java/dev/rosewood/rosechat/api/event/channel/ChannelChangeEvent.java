package dev.rosewood.rosechat.api.event.channel;

import dev.rosewood.rosechat.chat.channel.Channel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ChannelChangeEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final Channel oldChannel;
    private Channel channel;
    private boolean cancelled;

    /**
     * Called when a player changes their chat channel.
     * @param oldChannel The previous {@link Channel} that the player was in.
     * @param channel The new {@link Channel} that the player will be moved to.
     * @param player The {@link Player} who is changing channel.
     */
    public ChannelChangeEvent(Channel oldChannel, Channel channel, Player player) {
        super(!Bukkit.isPrimaryThread());

        this.player = player;
        this.oldChannel = oldChannel;
        this.channel = channel;
    }

    /**
     * @return The player who is changing channel.
     */
    public Player getPlayer() {
        return this.player;
    }

    /**
     * @return The previous channel that the player was in.
     */
    public Channel getOldChannel() {
        return this.oldChannel;
    }

    /**
     * @return The new channel that the player will be moved to.
     */
    public Channel getChannel() {
        return this.channel;
    }

    /**
     * Updates the channel that the player will be moved to.
     * @param channel The channel that the player will be moved to.
     */
    public void setChannel(Channel channel) {
        this.channel = channel;
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

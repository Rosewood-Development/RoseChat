package dev.rosewood.rosechat.api.event.player;

import dev.rosewood.rosechat.message.RosePlayer;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RosePlayerEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final RosePlayer player;
    private boolean cancelled;

    public RosePlayerEvent(RosePlayer player) {
        super(!Bukkit.isPrimaryThread());
        this.player = player;
    }

    public RosePlayer getPlayer() {
        return this.player;
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
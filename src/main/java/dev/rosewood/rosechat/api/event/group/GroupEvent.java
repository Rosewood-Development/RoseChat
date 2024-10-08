package dev.rosewood.rosechat.api.event.group;

import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@SuppressWarnings("unused")
public class GroupEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final GroupChannel group;
    private boolean cancelled;

    public GroupEvent(GroupChannel group) {
        super(!Bukkit.isPrimaryThread());

        this.group = group;
    }

    /**
     * @return The {@link GroupChannel} being used.
     */
    public GroupChannel getGroup() {
        return this.group;
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

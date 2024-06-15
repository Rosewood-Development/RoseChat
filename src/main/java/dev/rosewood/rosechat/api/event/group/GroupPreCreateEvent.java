package dev.rosewood.rosechat.api.event.group;

import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

@SuppressWarnings("unused")
public class GroupPreCreateEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final String id;
    private String name;
    private boolean cancelled;

    /**
     * Called before a {@link GroupChannel} is created.
     * @param player The {@link Player} who is creating the group.
     * @param id The ID of the group.
     * @param name The name of the group.
     */
    public GroupPreCreateEvent(Player player, String id, String name) {
        super(player);

        this.id = id;
        this.name = name;
    }

    /**
     * @return The ID of the {@link GroupChannel}.
     */
    public String getId() {
        return this.id;
    }

    /**
     * @return The name of the {@link GroupChannel}.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Updates the name of the {@link GroupChannel}.
     * @param name The new name to use.
     */
    public void setName(String name) {
        this.name = name;
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

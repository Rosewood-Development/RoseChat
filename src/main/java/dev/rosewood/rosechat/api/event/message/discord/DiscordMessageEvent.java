package dev.rosewood.rosechat.api.event.message.discord;

import dev.rosewood.rosechat.message.RoseMessage;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@SuppressWarnings("unused")
public class DiscordMessageEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final RoseMessage message;
    private final TextChannel textChannel;
    private boolean cancelled;

    public DiscordMessageEvent(RoseMessage message, TextChannel textChannel) {
        super(!Bukkit.isPrimaryThread());

        this.message = message;
        this.textChannel = textChannel;
    }

    /**
     * @return The {@link RoseMessage} being sent.
     */
    public RoseMessage getMessage() {
        return this.message;
    }

    /**
     * @return The {@link TextChannel} for the message.
     */
    public TextChannel getTextChannel() {
        return this.textChannel;
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

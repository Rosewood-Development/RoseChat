package dev.rosewood.rosechat.api.event.message;

import dev.rosewood.rosechat.message.RoseMessage;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.contents.MessageContents;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MessageReceivedEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final RoseMessage message;
    private final MessageContents contents;
    private final RosePlayer viewer;

    public MessageReceivedEvent(RoseMessage message, MessageContents contents, RosePlayer viewer) {
        super(!Bukkit.isPrimaryThread());

        this.message = message;
        this.contents = contents;
        this.viewer = viewer;
    }

    public RoseMessage getMessage() {
        return this.message;
    }

    public MessageContents getContents() {
        return this.contents;
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

}

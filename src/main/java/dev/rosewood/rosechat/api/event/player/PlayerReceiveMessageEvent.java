package dev.rosewood.rosechat.api.event.player;

import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.contents.MessageContents;
import dev.rosewood.rosechat.message.RoseMessage;

public class PlayerReceiveMessageEvent extends PlayerMessageEvent {

    private MessageContents contents;

    /**
     * Called when a player receives a private message.
     * @param sender The {@link RosePlayer} who sent the message.
     * @param receiver The {@link RosePlayer} who is receiving the message.
     * @param message The {@link RoseMessage} that is being sent.
     * @param contents The {@link MessageContents} for the message.
     */
    public PlayerReceiveMessageEvent(RosePlayer sender, RosePlayer receiver, RoseMessage message, MessageContents contents) {
        super(sender, receiver, message);

        this.contents = contents;
    }

    /**
     * @return The {@link MessageContents} for the parsed message.
     */
    public MessageContents getContents() {
        return this.contents;
    }

    /**
     * Sets the {@link MessageContents} for the parsed message.
     * @param contents The {@link MessageContents} to use.
     */
    public void setContents(MessageContents contents) {
        this.contents = contents;
    }

}

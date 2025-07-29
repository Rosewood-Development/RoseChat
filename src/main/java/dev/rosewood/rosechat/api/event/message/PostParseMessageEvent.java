package dev.rosewood.rosechat.api.event.message;

import dev.rosewood.rosechat.message.MessageDirection;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.contents.MessageContents;
import dev.rosewood.rosechat.message.RoseMessage;
import net.md_5.bungee.api.chat.BaseComponent;

public class PostParseMessageEvent extends MessageEvent {

    private MessageContents contents;

    /**
     * Called after a message has been parsed.
     * @param message The {@link RoseMessage} for the message that will be parsed.
     * @param viewer The {@link RosePlayer} for the person viewing the message.
     * @param direction Where this message is going.
     * @param contents The {@link MessageContents} for the parsed message.
     */
    public PostParseMessageEvent(RoseMessage message, RosePlayer viewer, MessageDirection direction, MessageContents contents) {
        super(message, viewer, direction);

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
     * @param contents The {@link MessageContents} for the parsed message.
     */
    public void setComponents(MessageContents contents) {
        this.contents = contents;
    }

}

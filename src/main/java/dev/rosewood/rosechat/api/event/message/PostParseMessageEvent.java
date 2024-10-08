package dev.rosewood.rosechat.api.event.message;

import dev.rosewood.rosechat.message.MessageDirection;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.wrapper.MessageTokenizerResults;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import net.md_5.bungee.api.chat.BaseComponent;

public class PostParseMessageEvent extends MessageEvent {

    private MessageTokenizerResults<BaseComponent[]> components;

    /**
     * Called after a message has been parsed.
     * @param message The {@link RoseMessage} for the message that will be parsed.
     * @param viewer The {@link RosePlayer} for the person viewing the message.
     * @param direction Where this message is going.
     * @param components The {@link MessageTokenizerResults} for the parsed message.
     */
    public PostParseMessageEvent(RoseMessage message, RosePlayer viewer, MessageDirection direction, MessageTokenizerResults<BaseComponent[]> components) {
        super(message, viewer, direction);

        this.components = components;
    }

    /**
     * @return The {@link MessageTokenizerResults} for the parsed message.
     */
    public MessageTokenizerResults<BaseComponent[]> getComponents() {
        return this.components;
    }

    /**
     * Sets the {@link MessageTokenizerResults} for the parsed message.
     * @param components The {@link MessageTokenizerResults} for the parsed message.
     */
    public void setComponents(MessageTokenizerResults<BaseComponent[]> components) {
        this.components = components;
    }

}

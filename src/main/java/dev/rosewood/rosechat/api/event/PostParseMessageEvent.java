package dev.rosewood.rosechat.api.event;

import dev.rosewood.rosechat.message.MessageDirection;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.wrapper.MessageTokenizerResults;
import net.md_5.bungee.api.chat.BaseComponent;

public class PostParseMessageEvent extends MessageEvent {

    private MessageTokenizerResults<BaseComponent[]> messageComponents;

    /**
     * Called after a message has been parsed.
     * @param message The {@link RoseMessage} for the message that will be parsed.
     * @param viewer The {@link RosePlayer} for the person viewing the message.
     * @param messageDirection Where this message is going.
     * @param messageComponents The {@link MessageTokenizerResults} for the parsed message.
     */
    public PostParseMessageEvent(RoseMessage message, RosePlayer viewer, MessageDirection messageDirection, MessageTokenizerResults<BaseComponent[]> messageComponents) {
        super(message, viewer, messageDirection);
        this.messageComponents = messageComponents;
    }

    /**
     * @return The {@link MessageTokenizerResults} for the parsed message.
     */
    public MessageTokenizerResults<BaseComponent[]> getMessageComponents() {
        return this.messageComponents;
    }

    /**
     * Sets the {@link MessageTokenizerResults} for the parsed message.
     * @param messageComponents The {@link MessageTokenizerResults} for the parsed message.
     */
    public void setMessageComponents(MessageTokenizerResults<BaseComponent[]> messageComponents) {
        this.messageComponents = messageComponents;
    }

}

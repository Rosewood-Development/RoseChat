package dev.rosewood.rosechat.api.event.player;

import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.wrapper.MessageTokenizerResults;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import net.md_5.bungee.api.chat.BaseComponent;

public class PlayerReceiveMessageEvent extends PlayerMessageEvent {

    private MessageTokenizerResults<BaseComponent[]> messageComponents;

    public PlayerReceiveMessageEvent(RosePlayer sender, RosePlayer receiver, RoseMessage message, MessageTokenizerResults<BaseComponent[]> messageComponents) {
        super(sender, receiver, message);
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

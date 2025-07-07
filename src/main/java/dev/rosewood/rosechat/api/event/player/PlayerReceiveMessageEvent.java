package dev.rosewood.rosechat.api.event.player;

import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.wrapper.MessageTokenizerResults;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import net.md_5.bungee.api.chat.BaseComponent;

public class PlayerReceiveMessageEvent extends PlayerMessageEvent {

    private MessageTokenizerResults components;

    /**
     * Called when a player receives a private message.
     * @param sender The {@link RosePlayer} who sent the message.
     * @param receiver The {@link RosePlayer} who is receiving the message.
     * @param message The {@link RoseMessage} that is being sent.
     * @param components The {@link MessageTokenizerResults} for the message.
     */
    public PlayerReceiveMessageEvent(RosePlayer sender, RosePlayer receiver, RoseMessage message, MessageTokenizerResults components) {
        super(sender, receiver, message);

        this.components = components;
    }

    /**
     * @return The {@link MessageTokenizerResults} for the parsed message.
     */
    public MessageTokenizerResults getComponents() {
        return this.components;
    }

    // TODO: Re-implement
//    /**
//     * Sets the {@link MessageTokenizerResults} for the parsed message.
//     * @param components The {@link MessageTokenizerResults} to use.
//     */
//    public void setComponents(MessageTokenizerResults components) {
//        this.components = components;
//    }

}

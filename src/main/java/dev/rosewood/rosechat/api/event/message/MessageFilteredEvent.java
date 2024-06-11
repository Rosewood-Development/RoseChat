package dev.rosewood.rosechat.api.event.message;

import dev.rosewood.rosechat.message.wrapper.MessageRules.RuleOutputs;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MessageFilteredEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final RoseMessage message;
    private final String rawMessage;
    private final RuleOutputs outputs;

    /**
     * Called when a message has been filtered.
     * @param message The {@link RoseMessage} that was filtered.
     * @param rawMessage The raw message that was sent by the player.
     * @param outputs The {@link RuleOutputs} containing why the message was filtered.
     */
    public MessageFilteredEvent(RoseMessage message, String rawMessage, RuleOutputs outputs) {
        super(!Bukkit.isPrimaryThread());

        this.message = message;
        this.rawMessage = rawMessage;
        this.outputs = outputs;
    }

    /**
     * @return The {@link RoseMessage} that was blocked.
     */
    public RoseMessage getMessage() {
        return this.message;
    }

    /**
     * @return The raw message that was sent by the player.
     */
    public String getRawMessage() {
        return this.rawMessage;
    }

    /**
     * @return The {@link RuleOutputs} containing why the message was blocked.
     */
    public RuleOutputs getOutputs() {
        return this.outputs;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}

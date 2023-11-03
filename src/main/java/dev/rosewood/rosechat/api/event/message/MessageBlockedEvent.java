package dev.rosewood.rosechat.api.event.message;


import dev.rosewood.rosechat.message.wrapper.MessageRules;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MessageBlockedEvent extends Event {


    private static final HandlerList HANDLERS = new HandlerList();

    private final RoseMessage message;
    private final String originalMessage;
    private final MessageRules.RuleOutputs outputs;

    public MessageBlockedEvent(RoseMessage message, String originalMessage, MessageRules.RuleOutputs outputs) {
        super(!Bukkit.isPrimaryThread());
        this.message = message;
        this.originalMessage = originalMessage;
        this.outputs = outputs;
    }

    public RoseMessage getMessage() {
        return this.message;
    }

    public String getOriginalMessage() {
        return this.originalMessage;
    }

    public MessageRules.RuleOutputs getOutputs() {
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

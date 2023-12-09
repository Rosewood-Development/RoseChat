package dev.rosewood.rosechat.message;

import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class MessageLog {

    private UUID owner;
    private List<String> messages;
    private final List<DeletableMessage> deletableMessages;
    private int cleanupAmount;

    public MessageLog(UUID sender) {
        this.owner = sender;
        this.messages = new ArrayList<>();
        this.deletableMessages = Collections.synchronizedList(new LinkedList<>());
    }

    public MessageLog(UUID sender, boolean doCleanup) {
        this(sender);
        if (doCleanup) {
            this.cleanupAmount = Setting.SPAM_MESSAGE_COUNT.getInt();
        }
    }

    /**
     * @param messageToAdd The message that was sent.
     * @return True if it is seen as spam.
     */
    public boolean addMessageWithSpamCheck(String messageToAdd) {
        // Refresh the cleanup amount, as it can be changed during a reload.
        this.cleanupAmount = Setting.SPAM_MESSAGE_COUNT.getInt();
        this.messages.add(messageToAdd);

        int similarMessages = 0;

        if (this.messages.size() > this.cleanupAmount - 1) {
            for (int i = 0; i < this.cleanupAmount; i++) {
                String message = this.messages.get((this.messages.size() - 1) - i);
                double difference = MessageUtils.getLevenshteinDistancePercent(message, messageToAdd);

                if ((1 - difference) <= (Setting.SPAM_FILTER_SENSITIVITY.getDouble() / 100)) {
                    similarMessages++;
                }
            }

            // Let's maybe not have an array size of... BIG.
            if (this.messages.size() > this.cleanupAmount * 2)
                this.messages.subList(0, this.cleanupAmount).clear();

            return similarMessages >= this.cleanupAmount;
        }

        return false;
    }

    public void addMessage(String message) {
        this.messages.add(message);
    }

    public UUID getOwner() {
        return this.owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public List<String> getMessages() {
        return this.messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    public List<DeletableMessage> getDeletableMessages() {
        return this.deletableMessages;
    }

    public boolean containsDeletableMessage(String json) {
        for (DeletableMessage message : this.deletableMessages) {
            if (json.equalsIgnoreCase(message.getJson())) return true;
        }
        return false;
    }

    public void addDeletableMessage(DeletableMessage deletableMessage) {
        if (this.deletableMessages.size() >= 100) this.deletableMessages.remove(0);
        this.deletableMessages.add(deletableMessage);
    }

    public int getCleanupAmount() {
        return this.cleanupAmount;
    }

    public void setCleanupAmount(int cleanupAmount) {
        this.cleanupAmount = cleanupAmount;
    }

}

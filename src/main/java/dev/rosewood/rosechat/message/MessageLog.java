package dev.rosewood.rosechat.message;

import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MessageLog {

    private UUID sender;
    private List<String> messages;
    private List<DeletableMessage> deletableMessages;
    private int cleanupAmount;

    public MessageLog(UUID sender) {
        this.messages = new ArrayList<>();
        this.deletableMessages = new ArrayList<>();
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
                double similarity = MessageUtils.getLevenshteinDistancePercent(message, messageToAdd);

                if (similarity >= Math.abs(Setting.SPAM_FILTER_SENSITIVITY.getDouble() - 1)) {
                    similarMessages++;
                }
            }

            // Let's maybe not have an array size of... BIG.
            if (this.messages.size() > this.cleanupAmount * 2) {
                for (int i = 0; i < this.cleanupAmount; i++) {
                    this.messages.remove(i);
                }
            }

            return similarMessages >= this.cleanupAmount;
        }

        return false;
    }

    public void addMessage(String message) {
        this.messages.add(message);
    }

    public UUID getSender() {
        return this.sender;
    }

    public void setSender(UUID sender) {
        this.sender = sender;
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

    public void addDeletableMessage(DeletableMessage deletableMessage) {
        this.deletableMessages.add(deletableMessage);
    }

    public int getCleanupAmount() {
        return this.cleanupAmount;
    }

    public void setCleanupAmount(int cleanupAmount) {
        this.cleanupAmount = cleanupAmount;
    }
}

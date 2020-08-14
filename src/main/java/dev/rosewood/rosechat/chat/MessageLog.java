package dev.rosewood.rosechat.chat;

import dev.rosewood.rosechat.RoseChat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MessageLog {

    private RoseChat plugin;
    private UUID sender;
    private List<String> messages;
    private int cleanupAmount;

    public MessageLog(UUID sender) {
        this.plugin = RoseChat.getInstance();
        this.messages = new ArrayList<>();
    }

    public MessageLog(UUID sender, boolean doCleanup) {
        this(sender);
        if (doCleanup)
            this.cleanupAmount = plugin.getConfigFile().getInt("moderation-settings.spam-message-count");
    }

    /**
     * @param messageToAdd The message that was sent.
     * @return True if it is seen as spam.
     */
    public boolean addMessageWithSpamCheck(String messageToAdd) {
        messages.add(messageToAdd);

        int similarMessages = 0;

        if (messages.size() > cleanupAmount - 1) {
            for (int i = 0; i < cleanupAmount; i++) {
                String message = messages.get((messages.size() - 1) - i);
                double similarity = MessageUtils.getLevenshteinDistancePercent(message, messageToAdd);

                if (similarity >= plugin.getConfigFile().getDouble("moderation-settings.spam-filter-sensitivity")) {
                    similarMessages++;
                }
            }

            // Let's maybe not have an array size of... BIG.
            if (messages.size() > cleanupAmount * 2) {
                for (int i = 0; i < cleanupAmount; i++) {
                    messages.remove(i);
                }
            }

            return similarMessages >= cleanupAmount;
        }

        return false;
    }

    public void addMessage(String message) {
        messages.add(message);
    }

    public UUID getSender() {
        return sender;
    }

    public void setSender(UUID sender) {
        this.sender = sender;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    public int getCleanupAmount() {
        return cleanupAmount;
    }

    public void setCleanupAmount(int cleanupAmount) {
        this.cleanupAmount = cleanupAmount;
    }
}

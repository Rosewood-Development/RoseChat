package dev.rosewood.rosechat.chat.log;

import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.DeletableMessage;
import dev.rosewood.rosechat.message.MessageUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class PlayerMessageLog extends ConsoleMessageLog {

    private UUID owner;
    private final List<DeletableMessage> deletableMessages;
    private int cleanupAmount;

    public PlayerMessageLog(UUID sender) {
        super();

        this.owner = sender;
        this.deletableMessages = Collections.synchronizedList(new LinkedList<>());
    }

    /**
     * @param messageToAdd The message that was sent.
     * @return True if it is seen as spam.
     */
    public boolean addMessageWithSpamCheck(String messageToAdd) {
        // Refresh the cleanup amount, as it can be changed during a reload.
        this.cleanupAmount = Setting.SPAM_MESSAGE_COUNT.getInt();
        this.addMessage(messageToAdd);

        int similarMessages = 0;

        if (this.messages.size() > this.cleanupAmount - 1) {
            for (int i = 0; i < this.cleanupAmount; i++) {
                String message = this.messages.get((this.messages.size() - 1) - i);
                double difference = MessageUtils.getLevenshteinDistancePercent(message, messageToAdd);

                if ((1 - difference) <= (Setting.SPAM_FILTER_SENSITIVITY.getDouble() / 100))
                    similarMessages++;
            }

            // Let's maybe not have an array size of... BIG.
            if (this.messages.size() > this.cleanupAmount * 2)
                this.messages.subList(0, this.cleanupAmount).clear();

            return similarMessages >= this.cleanupAmount;
        }

        return false;
    }

    public UUID getOwner() {
        return this.owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public List<DeletableMessage> getDeletableMessages() {
        return this.deletableMessages;
    }

    public DeletableMessage getDeletableMessage(String json) {
        if (this.deletableMessages.isEmpty())
            return null;

        BaseComponent[] one = ComponentSerializer.parse(json);
        if (one.length == 0)
            return null;
        BaseComponent componentOne = one[0];

        for (int i = this.deletableMessages.size() - 1; i >= 0; i--) {
            DeletableMessage message = this.deletableMessages.get(i);

            BaseComponent[] two = ComponentSerializer.parse(message.getJson());
            if (two.length == 0)
                continue;

            BaseComponent componentTwo = two[0];

            if (componentOne.toLegacyText().equalsIgnoreCase(componentTwo.toLegacyText()))
                return message;
        }

        return null;
    }

    public void addDeletableMessage(DeletableMessage deletableMessage) {
        if (this.deletableMessages.size() >= 100)
            this.deletableMessages.remove(0);

        this.deletableMessages.add(deletableMessage);
    }

    public int getCleanupAmount() {
        return this.cleanupAmount;
    }

    public void setCleanupAmount(int cleanupAmount) {
        this.cleanupAmount = cleanupAmount;
    }

}

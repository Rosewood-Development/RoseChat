package dev.rosewood.rosechat.managers;

import dev.rosewood.rosechat.chat.MessageLog;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.manager.AbstractDataManager;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataManager extends AbstractDataManager {

    private Map<UUID, MessageLog> playerChatMessages;
    private Map<UUID, UUID> lastMessagedBy;

    public DataManager(RosePlugin rosePlugin) {
        super(rosePlugin);
        this.playerChatMessages = new HashMap<>();
        this.lastMessagedBy = new HashMap<>();
    }

    public void createPlayerMessageLog(Player player) {
        this.createPlayerMessageLog(player.getUniqueId());
    }

    public void createPlayerMessageLog(UUID uuid) {
        playerChatMessages.put(uuid, new MessageLog(uuid, true));
    }

    public MessageLog getPlayerChatMessages(Player player) {
        return this.getPlayerChatMessage(player.getUniqueId());
    }

    public MessageLog getPlayerChatMessage(UUID uuid) {
        return playerChatMessages.get(uuid);
    }

    public void setPlayerChatMessages(Map<UUID, MessageLog> playerChatMessages) {
        this.playerChatMessages = playerChatMessages;
    }

    public Map<UUID, MessageLog> getPlayerChatMessages() {
        return playerChatMessages;
    }

    public UUID getLastMessagedBy(Player player) {
        return this.getLastMessagedBy(player.getUniqueId());
    }

    public UUID getLastMessagedBy(UUID uuid) {
        return this.lastMessagedBy.get(uuid);
    }

    public void setLastMessagedBy(Player player, Player recipient) {
        this.setLastMessagedBy(player.getUniqueId(), recipient.getUniqueId());
    }

    public void setLastMessagedBy(UUID uuid, UUID recipient) {
        this.lastMessagedBy.put(uuid, recipient);
    }

    public Map<UUID, UUID> getLastMessagedBy() {
        return lastMessagedBy;
    }

    public void setLastMessagedBy(Map<UUID, UUID> lastMessagedBy) {
        this.lastMessagedBy = lastMessagedBy;
    }
}

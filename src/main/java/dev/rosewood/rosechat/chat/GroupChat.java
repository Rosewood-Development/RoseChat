package dev.rosewood.rosechat.chat;

import dev.rosewood.rosechat.message.MessageWrapper;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GroupChat implements GroupReceiver {

    private final UUID uuid;
    private String name;
    private UUID owner;
    private List<UUID> members;

    /**
     * Creates a new group chat with a UUID.
     */
    public GroupChat(UUID owner) {
        this.uuid = owner;
        this.owner = owner;
        this.members = new ArrayList<>();
    }

    @Override
    public void send(MessageWrapper messageWrapper) {

    }

    @Override
    public List<UUID> getMembers() {
        return this.members;
    }

    /**
     * Gets the UUID of the group chat.
     * @return The UUID.
     */
    public UUID getUuid() {
        return this.uuid;
    }

    public void setMembers(List<UUID> members) {
        this.members = members;
    }

    /**
     * Adds a player to the group chat.
     * @param player The player to add.
     */
    public void addMember(Player player) {
        this.addMember(player.getUniqueId());
    }

    /**
     * Adds a UUID to the group chat.
     * @param uuid The UUID to add.
     */
    public void addMember(UUID uuid) {
        this.members.add(uuid);
    }

    /**
     * Removes a player from the group chat.
     * @param player The player to remove.
     */
    public void removeMember(Player player) {
        this.removeMember(player.getUniqueId());
    }

    /**
     * Removes a UUID from the group chat.
     * @param uuid The UUID to remove.
     */
    public void removeMember(UUID uuid) {
        this.members.remove(uuid);
    }

    /**
     * Gets the name of the group chat.
     * @return The name of the group chat.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name of the group chat.
     * @param name The name to use.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the owner of the group chat.
     * @return The owner of the group chat.
     */
    public UUID getOwner() {
        return this.owner;
    }

    /**
     * Sets the owner of the group chat.
     * @param owner The UUID to use.
     */
    public void setOwner(UUID owner) {
        this.owner = owner;
    }
}

package dev.rosewood.rosechat.chat;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.manager.GroupManager;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GroupChat implements Group {

    private final String id;
    private String name;
    private UUID owner;
    private List<UUID> members;

    /**
     * Creates a new group chat with an ID.
     */
    public GroupChat(String id) {
        this.id = id;
        this.members = new ArrayList<>();
    }


    @Override
    public void send(MessageWrapper message) {
        for (UUID uuid : this.members) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                PlayerData data = RoseChatAPI.getInstance().getPlayerData(player.getUniqueId());
                if (data.getIgnoringPlayers().contains(message.getSender().getUUID())) continue;

                player.spigot().sendMessage(message.parse(Setting.GROUP_FORMAT.getString(), new RoseSender(player)));
            }
        }

        for (UUID uuid : RoseChatAPI.getInstance().getDataManager().getGroupSpies()) {
            if (!this.members.contains(uuid)) {
                Player spy = Bukkit.getPlayer(uuid);
                if (spy != null) spy.spigot().sendMessage(message.parse(Setting.GROUP_SPY_FORMAT.getString(), new RoseSender(spy)));
            }
        }
    }

    @Override
    public void sendJson(String sender, UUID senderUUID, String senderGroup, String rawMessage) {

    }

    @Override
    public void sendFromDiscord(String id, MessageWrapper message) {

    }

    @Override
    public List<UUID> getMembers() {
        return this.members;
    }

    @Override
    public String getLocationPermission() {
        return "group";
    }

    /**
     * Saves the group chat.
     */
    public void save() {
        RoseChat.getInstance().getManager(GroupManager.class).createOrUpdateGroupChat(this);
    }

    /**
     * Sets the members in the group chat.
     * @param members The members list to be used.
     */
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
     * @return The ID of the group chat.
     */
    public String getId() {
        return id;
    }

    /**
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

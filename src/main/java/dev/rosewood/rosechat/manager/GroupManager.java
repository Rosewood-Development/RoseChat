package dev.rosewood.rosechat.manager;

import dev.rosewood.rosechat.chat.GroupChat;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.manager.AbstractDataManager;
import org.bukkit.Bukkit;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GroupManager extends AbstractDataManager {

    private final Map<UUID, GroupChat> groupChats;

    public GroupManager(RosePlugin rosePlugin) {
        super(rosePlugin);
        this.groupChats = new HashMap<>();
    }

    public GroupChat getGroupChat(UUID uuid) {
        GroupChat groupChat = this.groupChats.get(uuid);
        /*if (groupChat == null)
            throw new IllegalStateException("GroupChat for [" + uuid + "] not yet loaded.");*/
        return groupChat;
    }

    public void loadMemberGroupChats(UUID member) {
        this.async(() -> {
            this.databaseConnector.connect(connection -> {
                String query = "SELECT gc.* FROM " + this.getTablePrefix() + "group_chat_member gcm " +
                        "JOIN " + this.getTablePrefix() + "group_chat gc ON gcm.group_chat = gc.owner WHERE gcm.uuid = ?";
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setString(1, member.toString());
                    ResultSet result = statement.executeQuery();

                    if (result.next()) {
                        String name = result.getString("name");
                        UUID owner = UUID.fromString(result.getString("owner"));
                        if (this.getGroupChat(owner) == null) {
                            GroupChat groupChat = new GroupChat(owner);
                            groupChat.setName(name);
                            this.groupChats.put(owner, groupChat);
                        }
                    }
                }
            });
        });
    }

    public void addMember(GroupChat groupChat, UUID member) {
        this.async(() -> {
            this.databaseConnector.connect(connection -> {
                String insertQuery = "INSERT INTO " + this.getTablePrefix() + "group_chat_member (group_chat, uuid) " +
                        "VALUES (?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
                    statement.setString(1, groupChat.getOwner().toString());
                    statement.setString(2, member.toString());
                    statement.executeUpdate();
                }
            });
        });
    }

    public void removeMember(GroupChat groupChat, UUID member) {
        this.async(() -> {
            this.databaseConnector.connect(connection -> {
                String deleteQuery = "DELETE FROM " + this.getTablePrefix() + "group_chat_member WHERE group_chat = ? AND uuid = ?";
                try (PreparedStatement statement = connection.prepareStatement(deleteQuery)) {
                    statement.setString(1, groupChat.getOwner().toString());
                    statement.setString(2, member.toString());
                    statement.executeUpdate();
                }
            });
        });
    }

    public void createOrUpdateGroupChat(GroupChat groupChat) {
        this.async(() -> {
            this.databaseConnector.connect(connection -> {
                boolean create;

                String checkQuery = "SELECT 1 FROM " + this.getTablePrefix() + "group_chat WHERE owner = ?";
                try (PreparedStatement statement = connection.prepareStatement(checkQuery)) {
                    statement.setString(1, groupChat.getOwner().toString());
                    ResultSet result = statement.executeQuery();
                    create = !result.next();
                }

                if (create) {
                    String insertQuery = "INSERT INTO " + this.getTablePrefix() + "group_chat (name, owner) " +
                            "VALUES (?, ?)";
                    try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
                        statement.setString(1, groupChat.getName());
                        statement.setString(2, groupChat.getOwner().toString());
                        statement.executeUpdate();
                    }
                } else {
                    String updateQuery = "UPDATE " + this.getTablePrefix() + "group_chat SET " +
                            "name = ? WHERE owner = ?";
                    try (PreparedStatement statement = connection.prepareStatement(updateQuery)) {
                        statement.setString(1, groupChat.getName());
                        statement.setString(2, groupChat.getOwner().toString());
                        statement.executeUpdate();
                    }
                }
            });
        });
    }

    public void deleteGroupChat(GroupChat groupChat) {
        this.async(() -> {
            this.databaseConnector.connect(connection -> {
                String deleteQuery = "DELETE FROM " + this.getTablePrefix() + "group_chat WHERE owner = ?";
                try (PreparedStatement statement = connection.prepareStatement(deleteQuery)) {
                    statement.setString(1, groupChat.getOwner().toString());
                    statement.executeUpdate();
                }

                String deleteMembersQuery = "DELETE FROM " + this.getTablePrefix() + "group_chat_member WHERE owner = ?";
                try (PreparedStatement statement = connection.prepareStatement(deleteMembersQuery)) {
                    statement.setString(1, groupChat.getOwner().toString());
                    statement.executeUpdate();
                }
            });
        });
    }

    private void async(Runnable asyncCallback) {
        Bukkit.getScheduler().runTaskAsynchronously(this.rosePlugin, asyncCallback);
    }

    private void sync(Runnable syncCallback) {
        Bukkit.getScheduler().runTask(this.rosePlugin, syncCallback);
    }

    public void addGroupChat(GroupChat groupChat) {
        this.groupChats.put(groupChat.getOwner(), groupChat);
    }

    public void removeGroupChat(GroupChat groupChat) {
        this.groupChats.remove(groupChat.getOwner());
    }

    public Map<UUID, GroupChat> getGroupChats() {
        return this.groupChats;
    }
}

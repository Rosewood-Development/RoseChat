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

    private final Map<String, GroupChat> groupChats;

    public GroupManager(RosePlugin rosePlugin) {
        super(rosePlugin);
        this.groupChats = new HashMap<>();
    }

    public GroupChat getGroupChatById(String id) {
        return this.groupChats.get(id);
    }

    public GroupChat getGroupChatByOwner(UUID owner) {
        for (GroupChat groupChat : this.groupChats.values()) {
            if (groupChat.getOwner().equals(owner)) return groupChat;
        }

        return null;
    }

    public void loadMemberGroupChats(UUID member) {
        this.async(() -> {
            this.databaseConnector.connect(connection -> {
                String query = "SELECT gc.* FROM " + this.getTablePrefix() + "group_chat_member gcm " +
                        "JOIN " + this.getTablePrefix() + "group_chat gc ON gcm.group_chat = gc.id WHERE gcm.uuid = ?";
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setString(1, member.toString());
                    ResultSet result = statement.executeQuery();

                    if (result.next()) {
                        String id = result.getString("id");
                        String name = result.getString("name");
                        UUID owner = UUID.fromString(result.getString("owner"));
                        GroupChat groupChat = this.getGroupChatById(id);
                        if (groupChat == null) {
                            groupChat = new GroupChat(id);
                            groupChat.setOwner(owner);
                            groupChat.addMember(owner);
                            groupChat.setName(name);
                            this.groupChats.put(id, groupChat);
                        }
                    }
                }
            });
        });
    }

    public void loadGroupChats() {
        this.async(() -> {
            this.databaseConnector.connect(connection -> {
                String query = "SELECT * FROM " + this.getTablePrefix() + "group_chat";
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    ResultSet result = statement.executeQuery();
                    if (result.next()) {
                        String id = result.getString("id");
                        String name = result.getString("name");
                        UUID owner = UUID.fromString(result.getString("owner"));
                        if (!this.groupChats.containsKey(id)) {
                            GroupChat groupChat = new GroupChat(id);
                            groupChat.setOwner(owner);
                            groupChat.setName(name);
                            this.groupChats.put(id, groupChat);
                        }
                    }
                }
            });
        });
    }

    public void loadMembers() {
        this.async(() -> {
            this.databaseConnector.connect(connection -> {
                String query = "SELECT * FROM " + this.getTablePrefix() + "group_chat_member";
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    ResultSet result = statement.executeQuery();
                    if (result.next()) {
                        String group = result.getString("group_chat");
                        UUID member = UUID.fromString(result.getString("uuid"));
                        if (this.groupChats.containsKey(group)) {
                            this.groupChats.get(group).addMember(member);
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
                    statement.setString(1, groupChat.getId());
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
                    statement.setString(1, groupChat.getId());
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

                String checkQuery = "SELECT 1 FROM " + this.getTablePrefix() + "group_chat WHERE id = ?";
                try (PreparedStatement statement = connection.prepareStatement(checkQuery)) {
                    statement.setString(1, groupChat.getId());
                    ResultSet result = statement.executeQuery();
                    create = !result.next();
                }

                if (create) {
                    String insertQuery = "INSERT INTO " + this.getTablePrefix() + "group_chat (id, name, owner) " +
                            "VALUES (?, ?, ?)";
                    try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
                        statement.setString(1, groupChat.getId());
                        statement.setString(2, groupChat.getName());
                        statement.setString(3, groupChat.getOwner().toString());
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
                String deleteQuery = "DELETE FROM " + this.getTablePrefix() + "group_chat WHERE id = ?";
                try (PreparedStatement statement = connection.prepareStatement(deleteQuery)) {
                    statement.setString(1, groupChat.getId());
                    statement.executeUpdate();
                }

                String deleteMembersQuery = "DELETE FROM " + this.getTablePrefix() + "group_chat_member WHERE group_chat = ?";
                try (PreparedStatement statement = connection.prepareStatement(deleteMembersQuery)) {
                    statement.setString(1, groupChat.getId());
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
        this.groupChats.put(groupChat.getId(), groupChat);
        groupChat.save();
    }

    public void removeGroupChat(GroupChat groupChat) {
        this.groupChats.remove(groupChat.getId());
    }

    public Map<String, GroupChat> getGroupChats() {
        return this.groupChats;
    }
}

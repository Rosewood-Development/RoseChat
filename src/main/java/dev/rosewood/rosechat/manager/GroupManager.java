package dev.rosewood.rosechat.manager;

import dev.rosewood.rosechat.chat.GroupChat;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.manager.Manager;
import org.bukkit.Bukkit;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class GroupManager extends Manager {

    private DataManager dataManager;
    private final Map<String, GroupChat> groupChats;

    public GroupManager(RosePlugin rosePlugin) {
        super(rosePlugin);
        this.dataManager = rosePlugin.getManager(DataManager.class);
        this.groupChats = new HashMap<>();
    }

    @Override
    public void reload() {

    }

    @Override
    public void disable() {

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

    // Why is this broke :(
    public void loadMemberGroupChats(UUID member) {
        this.async(() -> {
            this.dataManager.getDatabaseConnector().connect(connection -> {
                List<GroupChat> gcs = new ArrayList<>();

                String groupQuery = "SELECT * FROM " + this.dataManager.getTablePrefix() + "group_chat_member gcm JOIN " +
                        this.dataManager.getTablePrefix() + "group_chat gc ON gc.id = gcm.group_chat WHERE gcm.uuid = ?";
                try (PreparedStatement statement = connection.prepareStatement(groupQuery)) {
                    statement.setString(1, member.toString());
                    ResultSet result = statement.executeQuery();

                    if (result.next()) {
                        GroupChat gc = new GroupChat(result.getString("id"));
                    }
                }
            });
        });
    }

    public void addMember(GroupChat groupChat, UUID member) {
        this.async(() -> {
            this.dataManager.getDatabaseConnector().connect(connection -> {
                String insertQuery = "INSERT INTO " + this.dataManager.getTablePrefix() + "group_chat_member (group_chat, uuid) " +
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
            this.dataManager.getDatabaseConnector().connect(connection -> {
                String deleteQuery = "DELETE FROM " + this.dataManager.getTablePrefix() + "group_chat_member WHERE group_chat = ? AND uuid = ?";
                try (PreparedStatement statement = connection.prepareStatement(deleteQuery)) {
                    statement.setString(1, groupChat.getId());
                    statement.setString(2, member.toString());
                    statement.executeUpdate();
                }
            });
        });
    }

    public void getMembers(String id, Consumer<List<UUID>> callback) {
        this.async(() -> {
            this.dataManager.getDatabaseConnector().connect(connection -> {
                List<UUID> members = new ArrayList<>();

                String membersQuery = "SELECT * FROM " + this.dataManager.getTablePrefix() + "group_chat_member WHERE group_chat = ?";
                try (PreparedStatement statement = connection.prepareStatement(membersQuery)) {
                    statement.setString(1, id);
                    ResultSet result = statement.executeQuery();

                    if (result.next()) {
                        members.add(UUID.fromString(result.getString("uuid")));
                    }
                }

                callback.accept(members);
            });
        });
    }

    public void createOrUpdateGroupChat(GroupChat groupChat) {
        this.async(() -> {
            this.dataManager.getDatabaseConnector().connect(connection -> {
                boolean create;

                String checkQuery = "SELECT 1 FROM " + this.dataManager.getTablePrefix() + "group_chat WHERE id = ?";
                try (PreparedStatement statement = connection.prepareStatement(checkQuery)) {
                    statement.setString(1, groupChat.getId());
                    ResultSet result = statement.executeQuery();
                    create = !result.next();
                }

                if (create) {
                    String insertQuery = "INSERT INTO " + this.dataManager.getTablePrefix() + "group_chat (id, name, owner) " +
                            "VALUES (?, ?, ?)";
                    try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
                        statement.setString(1, groupChat.getId());
                        statement.setString(2, groupChat.getName());
                        statement.setString(3, groupChat.getOwner().toString());
                        statement.executeUpdate();
                    }
                } else {
                    String updateQuery = "UPDATE " + this.dataManager.getTablePrefix() + "group_chat SET " +
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
            this.dataManager.getDatabaseConnector().connect(connection -> {
                String deleteQuery = "DELETE FROM " + this.dataManager.getTablePrefix() + "group_chat WHERE id = ?";
                try (PreparedStatement statement = connection.prepareStatement(deleteQuery)) {
                    statement.setString(1, groupChat.getId());
                    statement.executeUpdate();
                }

                String deleteMembersQuery = "DELETE FROM " + this.dataManager.getTablePrefix() + "group_chat_member WHERE group_chat = ?";
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

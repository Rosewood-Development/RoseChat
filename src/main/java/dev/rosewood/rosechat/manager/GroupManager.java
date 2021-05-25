package dev.rosewood.rosechat.manager;

import dev.rosewood.rosechat.chat.GroupChat;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.manager.AbstractDataManager;
import org.bukkit.Bukkit;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class GroupManager extends AbstractDataManager {

    private Map<UUID, GroupChat> groupChats;

    public GroupManager(RosePlugin rosePlugin) {
        super(rosePlugin);
        this.groupChats = new HashMap<>();
    }

    public GroupChat getGroupChat(UUID uuid) {
        GroupChat groupChat = this.groupChats.get(uuid);
        if (groupChat == null)
            throw new IllegalStateException("GroupChat for [" + uuid + "] not yet loaded.");
        return groupChat;
    }

    public void unloadGroupChat(UUID uuid) {
        this.groupChats.remove(uuid);
    }

    public void getGroupChat(UUID uuid, Consumer<GroupChat> callback) {
        if (this.groupChats.containsKey(uuid)) {
            callback.accept(this.groupChats.get(uuid));
            return;
        }

        this.async(() -> {
            this.databaseConnector.connect(connection -> {
                String dataQuery = "SELECT * FROM " + this.getTablePrefix() + "group_chats WHERE uuid = ?";
                try (PreparedStatement statement = connection.prepareStatement(dataQuery)) {
                    statement.setString(1, uuid.toString());

                    ResultSet result = statement.executeQuery();
                    if (result.next()) {
                        String name = result.getString("name");
                        List<UUID> members = new ArrayList<>();

                        for (String memberStr : result.getString("members").split(",")) {
                            members.add(UUID.fromString(memberStr));
                        }

                        GroupChat groupChat = new GroupChat(uuid);
                        groupChat.setName(name);
                        groupChat.setMembers(members);
                        this.groupChats.put(uuid, groupChat);
                        callback.accept(groupChat);
                    } else {
                        GroupChat groupChat = new GroupChat(uuid);
                        this.groupChats.put(uuid, groupChat);
                        callback.accept(groupChat);
                    }
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
        this.groupChats.put(groupChat.getUuid(), groupChat);
    }

    public void removeGroupChat(GroupChat groupChat) {
        this.groupChats.remove(groupChat.getUuid());
    }

    public Map<UUID, GroupChat> getGroupChats() {
        return this.groupChats;
    }

    public GroupChat getGroupChatByOwner(UUID owner) {
        for (GroupChat groupChat : this.groupChats.values()) {
            if (groupChat.getOwner().equals(owner)) {
                return groupChat;
            }
        }

        return null;
    }
}

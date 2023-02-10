package dev.rosewood.rosechat.manager;

import dev.rosewood.rosechat.chat.GroupChat;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.manager.Manager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import org.bukkit.Bukkit;

public class GroupManager extends Manager {

    private final DataManager dataManager;
    private final Map<String, GroupChat> groupChats;
    private final List<String> groupChatNames;

    public GroupManager(RosePlugin rosePlugin) {
        super(rosePlugin);
        this.dataManager = rosePlugin.getManager(DataManager.class);
        this.groupChats = new HashMap<>();
        this.groupChatNames = new ArrayList<>();
    }

    @Override
    public void reload() {
       // Bukkit.getOnlinePlayers().forEach(player -> this.loadMemberGroupChats(player.getUniqueId()));
       // this.loadNames();
    }

    @Override
    public void disable() {
        this.groupChats.clear();
        this.groupChatNames.clear();
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

    /*public void loadMemberGroupChats(UUID member) {
        Bukkit.getScheduler().runTaskAsynchronously(this.rosePlugin, () -> {
            List<GroupChat> groupChats = this.dataManager.getMemberGroupChats(member);
            for (GroupChat groupChat : groupChats) {
                if (!this.groupChats.containsKey(groupChat.getId()))
                    this.groupChats.put(groupChat.getId(), groupChat);
            }
        });
    }

    public void loadNames() {
        Bukkit.getScheduler().runTaskAsynchronously(this.rosePlugin, () -> {
            List<String> groupChatNames = this.dataManager.getGroupChatNames();
            this.groupChatNames.addAll(groupChatNames);
        });
    }

    public void addMember(GroupChat groupChat, UUID member) {
        Bukkit.getScheduler().runTaskAsynchronously(this.rosePlugin, () -> this.dataManager.addGroupChatMember(groupChat, member));
    }

    public void removeMember(GroupChat groupChat, UUID member) {
        Bukkit.getScheduler().runTaskAsynchronously(this.rosePlugin, () -> this.dataManager.removeGroupChatMember(groupChat, member));
    }

    public void getMembers(String id, Consumer<List<UUID>> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(this.rosePlugin, () -> {
            List<UUID> members = this.dataManager.getGroupChatMembers(id);
            callback.accept(members);
        });
    }

    public void createOrUpdateGroupChat(GroupChat groupChat) {
        Bukkit.getScheduler().runTaskAsynchronously(this.rosePlugin, () -> this.dataManager.createOrUpdateGroupChat(groupChat));
    }

    public void deleteGroupChat(GroupChat groupChat) {
        Bukkit.getScheduler().runTaskAsynchronously(this.rosePlugin, () -> {
            this.dataManager.deleteGroupChat(groupChat);
            this.groupChats.remove(groupChat.getId());
        });
    }

    public void getGroupInfo(String groupId, Consumer<GroupInfo> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(this.rosePlugin, () -> {
            GroupInfo groupInfo = this.dataManager.getGroupInfo(groupId);
            callback.accept(groupInfo);
        });
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

    public List<String> getGroupChatNames() {
        return this.groupChatNames;
    }*/

    public static class GroupInfo {

        private final String id;
        private final String name;
        private final String owner;
        private final int members;

        public GroupInfo(String id, String name, String owner, int members) {
            this.id = id;
            this.name = name;
            this.owner = owner;
            this.members = members;
        }

        public String getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        public String getOwner() {
            return this.owner;
        }

        public int getMembers() {
            return this.members;
        }
    }

}

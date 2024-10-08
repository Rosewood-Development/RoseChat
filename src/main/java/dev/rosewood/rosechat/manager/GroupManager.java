package dev.rosewood.rosechat.manager;

import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;
import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannelProvider;
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

    private final GroupChannelProvider channelProvider;
    private final Map<String, GroupChannel> groupChats;
    private final List<String> groupChatIDs;
    private DataManager dataManager;

    public GroupManager(RosePlugin rosePlugin) {
        super(rosePlugin);

        this.channelProvider = new GroupChannelProvider();
        this.groupChats = new HashMap<>();
        this.groupChatIDs = new ArrayList<>();
    }

    @Override
    public void reload() {
        this.dataManager = rosePlugin.getManager(DataManager.class);
        Bukkit.getOnlinePlayers().forEach(player -> this.loadMemberGroupChats(player.getUniqueId(), (gcs) -> {}));
        this.loadNames();
    }

    @Override
    public void disable() {
        this.groupChats.clear();
        this.groupChatIDs.clear();
    }

    public GroupChannel getGroupChatById(String id) {
        return this.groupChats.get(id);
    }

    public GroupChannel getGroupChatByOwner(UUID owner) {
        for (GroupChannel groupChat : this.groupChats.values()) {
            if (groupChat.getOwner().equals(owner))
                return groupChat;
        }

        return null;
    }

    public void loadGroupChat(String id, Consumer<GroupChannel> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(this.rosePlugin, () -> {
            GroupChannel group = this.dataManager.getGroupChannel(id);
            List<UUID> members = this.dataManager.getGroupChatMembers(id);
            group.setMembers(members);
            callback.accept(group);
        });
    }

    public void loadMemberGroupChats(UUID member, Consumer<List<GroupChannel>> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(this.rosePlugin, () -> {
            List<GroupChannel> groupChats = this.dataManager.getMemberGroupChats(member);
            for (GroupChannel groupChat : groupChats) {
                if (!this.groupChats.containsKey(groupChat.getId()))
                    this.groupChats.put(groupChat.getId(), groupChat);
            }

            callback.accept(groupChats);
        });
    }

    public void loadNames() {
        Bukkit.getScheduler().runTaskAsynchronously(this.rosePlugin, () -> {
            List<String> groupChatNames = this.dataManager.getGroupChatNames();
            this.groupChatIDs.addAll(groupChatNames);
        });
    }

    public void addMember(GroupChannel groupChat, UUID member) {
        Bukkit.getScheduler().runTaskAsynchronously(this.rosePlugin, () -> this.dataManager.addGroupChatMember(groupChat, member));
    }

    public void removeMember(GroupChannel groupChat, UUID member) {
        Bukkit.getScheduler().runTaskAsynchronously(this.rosePlugin, () -> this.dataManager.removeGroupChatMember(groupChat, member));
    }

    public void getMembers(String id, Consumer<List<UUID>> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(this.rosePlugin, () -> {
            List<UUID> members = this.dataManager.getGroupChatMembers(id);
            callback.accept(members);
        });
    }

    public void createOrUpdateGroupChat(GroupChannel groupChat) {
        Bukkit.getScheduler().runTaskAsynchronously(this.rosePlugin, () -> this.dataManager.createOrUpdateGroupChat(groupChat));
    }

    public void deleteGroupChat(GroupChannel groupChat) {
        Bukkit.getScheduler().runTaskAsynchronously(this.rosePlugin, () -> {
            this.dataManager.deleteGroupChat(groupChat);
            this.groupChats.remove(groupChat.getId());
            this.groupChatIDs.remove(groupChat.getId());
        });
    }

    public void getGroupInfo(String groupId, Consumer<GroupInfo> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(this.rosePlugin, () -> {
            GroupInfo groupInfo = this.dataManager.getGroupInfo(groupId);
            callback.accept(groupInfo);
        });
    }

    public void getAllGroupInfo(Consumer<List<GroupInfo>> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(this.rosePlugin, () -> {
            List<GroupInfo> groupInfo = this.dataManager.getAllGroupInfo();
            callback.accept(groupInfo);
        });
    }

    public void addGroupChat(GroupChannel groupChat) {
        this.groupChats.put(groupChat.getId(), groupChat);
        this.groupChatIDs.add(groupChat.getId());
        groupChat.save();
    }

    public void removeGroupChat(GroupChannel groupChat) {
        this.groupChats.remove(groupChat.getId());
    }

    public Map<String, GroupChannel> getGroupChats() {
        return this.groupChats;
    }

    public List<String> getGroupChatIDs() {
        return this.groupChatIDs;
    }

    public GroupChannelProvider getChannelProvider() {
        return this.channelProvider;
    }

    public record GroupInfo(String id, String name, String owner, int members) { }

}

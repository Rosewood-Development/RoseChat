package dev.rosewood.rosechat.hook.channel.rosechat;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.api.event.group.GroupDisbandEvent;
import dev.rosewood.rosechat.api.event.group.GroupJoinEvent;
import dev.rosewood.rosechat.api.event.group.GroupLeaveEvent;
import dev.rosewood.rosechat.api.event.group.GroupNameEvent;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.chat.channel.ChannelMessageOptions;
import dev.rosewood.rosechat.chat.channel.ChannelSettings;
import dev.rosewood.rosechat.config.Settings;
import dev.rosewood.rosechat.manager.GroupManager;
import dev.rosewood.rosechat.message.PermissionArea;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.wrapper.MessageRules;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class GroupChannel extends Channel {

    private String name;
    private UUID owner;

    public GroupChannel(String id) {
        super(null);

        this.id = id;

        this.settings = new ChannelSettings();
        this.getSettings().getFormats().put("chat", Settings.GROUP_FORMAT.get());
    }

    public void save() {
        RoseChat.getInstance().getManager(GroupManager.class).createOrUpdateGroupChat(this);
    }

    @Override
    public void send(ChannelMessageOptions options) {
        // An empty message can't be sent to the channel.
        if (options.message() == null)
            return;

        RoseChatAPI api = RoseChatAPI.getInstance();

        // If there is no sender, the message can't be sent a group channel.
        if (options.sender() == null)
            return;

        // Handle messages to be sent to this group.
        RoseMessage message = RoseMessage.forLocation(options.sender(), PermissionArea.GROUP);
        message.setPlaceholders(this.getInfoPlaceholders().build());

        // Apply the rules for this message or return if the message was blocked.
        MessageRules rules = this.applyRules(message, options.message());
        if (rules == null)
            return;

        String format = options.format() != null ? options.format() : this.getSettings().getFormats().get("chat");

        // Get all the members as RosePlayers and add the console.
        List<RosePlayer> members = this.getMembers().stream()
                .map(Bukkit::getPlayer).filter(Objects::nonNull).map(RosePlayer::new).collect(Collectors.toList());
        members.add(new RosePlayer(Bukkit.getConsoleSender()));

        for (RosePlayer member : members) {
            // Check if the player can receive the message.
            if (!this.canPlayerReceiveMessage(message.getSender(), member))
                continue;

            RoseChat.MESSAGE_THREAD_POOL.execute(() ->
                    member.send(message.parse(member, format).content()));
        }

        // Send the message to the spies.
        for (UUID uuid : api.getPlayerDataManager().getGroupSpies()) {
            // Continue if the spy is also a member.
            if (this.members.contains(uuid))
                continue;

            Player player = Bukkit.getPlayer(uuid);
            if (player == null)
                continue;

            RosePlayer rosePlayer = new RosePlayer(player);
            RoseChat.MESSAGE_THREAD_POOL.execute(() ->
                    rosePlayer.send(message.parse(rosePlayer, Settings.GROUP_SPY_FORMAT.get()).content()));
        }
    }

    @Override
    public boolean canPlayerReceiveMessage(RosePlayer sender, RosePlayer receiver) {
        if (sender.getUUID() != null && receiver.getUUID() != null)
            if (sender.getUUID().equals(receiver.getUUID()))
                return true;

        PlayerData data = receiver.getPlayerData();
        if (data == null)
            return true;

        if (sender.getUUID() != null)
            return !data.getIgnoringPlayers().contains(sender.getUUID());

        return true;
    }

    private void addMember(UUID uuid) {
        this.members.add(uuid);
    }

    private void removeMember(UUID uuid) {
        this.members.remove(uuid);
    }

    @Override
    public List<UUID> getMembers() {
        return this.members;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public List<String> getServers() {
        return Collections.emptyList();
    }

    @Override
    public boolean canJoinByCommand(RosePlayer player) {
        return Settings.CAN_JOIN_GROUP_CHANNELS.get();
    }

    @Override
    public StringPlaceholders.Builder getInfoPlaceholders() {
        return super.getInfoPlaceholders()
                .add("owner", Bukkit.getOfflinePlayer(this.owner).getName())
                .add("name", this.name)
                .add("group_id", this.getId())
                .add("group", this.getId())
                .add("group_owner", Bukkit.getOfflinePlayer(this.owner).getName())
                .add("group_name", this.name);
    }

    // Group Methods

    public boolean rename(String name) {
        GroupNameEvent event = new GroupNameEvent(this, name);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return false;

        name = event.getName();

        this.setName(name);
        this.save();

        return true;
    }

    public boolean join(UUID uuid) {
        GroupJoinEvent event = new GroupJoinEvent(this, Bukkit.getPlayer(uuid));
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return false;

        this.addMember(uuid);
        RoseChatAPI.getInstance().getGroupManager().addMember(this, uuid);
        return true;
    }

    public boolean kick(UUID uuid, boolean wasKicked) {
        GroupLeaveEvent event = new GroupLeaveEvent(this, Bukkit.getOfflinePlayer(uuid), wasKicked);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return false;

        this.removeMember(uuid);
        RoseChatAPI.getInstance().getGroupManager().removeMember(this, uuid);
        return true;
    }

    public boolean disband() {
        GroupDisbandEvent event = new GroupDisbandEvent(this);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return false;

        RoseChatAPI.getInstance().deleteGroupChat(this);
        return true;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public UUID getOwner() {
        return this.owner;
    }

    public void setMembers(List<UUID> members) {
        this.members.addAll(members);
    }

}

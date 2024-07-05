package dev.rosewood.rosechat.hook.channel.rosechat;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.api.event.group.GroupDisbandEvent;
import dev.rosewood.rosechat.api.event.group.GroupJoinEvent;
import dev.rosewood.rosechat.api.event.group.GroupLeaveEvent;
import dev.rosewood.rosechat.api.event.group.GroupNameEvent;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.chat.channel.FormatGroup;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
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

        FormatGroup formats = new FormatGroup();
        formats.add("minecraft", Setting.GROUP_FORMAT.getString());
        this.setFormats(formats);
    }

    public void save() {
        RoseChat.getInstance().getManager(GroupManager.class).createOrUpdateGroupChat(this);
    }

    @Override
    public void send(RosePlayer sender, String message) {
        // Parses the first message synchronously
        // Allows for creating a token storage.
        RoseMessage roseMessage = RoseMessage.forLocation(sender, PermissionArea.GROUP);
        roseMessage.setPlayerInput(message);
        roseMessage.setPlaceholders(this.getInfoPlaceholders().build());

        // Create the rules for this message.
        MessageRules rules = new MessageRules().applyAllFilters();
        MessageRules.RuleOutputs ruleOutputs = rules.apply(roseMessage, message);

        if (ruleOutputs.getWarning() != null)
            ruleOutputs.getWarning().send(sender);

        // Check if the message is allowed to be sent.
        if (ruleOutputs.isBlocked())
            return;

        List<RosePlayer> receivers = this.getMembers().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).map(RosePlayer::new).collect(Collectors.toList());
        receivers.add(new RosePlayer(Bukkit.getConsoleSender()));

        for (RosePlayer receiver : receivers) {
            // Clone the message for viewer-specific placeholders.
            PlayerData playerData = receiver.getPlayerData();
            if (playerData != null && !this.canPlayerReceiveMessage(receiver, playerData, sender.getUUID()))
                continue;

            RoseChat.MESSAGE_THREAD_POOL.execute(() -> {
                receiver.send(roseMessage.parse(receiver, this.getFormats().getMinecraft()).content());
            });
        }

        for (UUID uuid : RoseChatAPI.getInstance().getPlayerDataManager().getGroupSpies()) {
            if (this.members.contains(uuid))
                continue;

            Player player = Bukkit.getPlayer(uuid);
            if (player == null)
                continue;

            RosePlayer rosePlayer = new RosePlayer(player);
            PlayerData playerData = RoseChatAPI.getInstance().getPlayerData(uuid);

            // Don't send the message if the receiver can't receive it.
            if (!this.canPlayerReceiveMessage(rosePlayer, playerData, sender.getUUID()))
                continue;

            RoseChat.MESSAGE_THREAD_POOL.execute(() -> {
                rosePlayer.send(roseMessage.parse(rosePlayer, Setting.GROUP_SPY_FORMAT.getString()).content());
            });
        }
    }

    @Override
    public void send(RosePlayer sender, String message, String format, boolean sendToDiscord) {
        this.send(sender, message);
    }

    @Override
    public void send(RoseMessage message, String discordId) {
        // No discord support for GroupChats
    }

    @Override
    public void send(RosePlayer sender, String message, UUID messageId, boolean isJson) {
        // No bungee support for GroupChats
    }

    @Override
    public boolean canPlayerReceiveMessage(RosePlayer receiver, PlayerData data, UUID senderUUID) {
        return (data != null
                && !data.getIgnoringPlayers().contains(senderUUID));
    }

    @Override
    public void onJoin(RosePlayer player) {
        // No implementation
    }

    @Override
    public void onLeave(RosePlayer player) {
        // No implementation
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
        return Setting.CAN_JOIN_GROUP_CHANNELS.getBoolean();
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

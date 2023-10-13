package dev.rosewood.rosechat.hook.channel.rosechat;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.manager.GroupManager;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.wrapper.MessageRules;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import java.util.Objects;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class GroupChannel extends Channel {

    private String name;
    private UUID owner;

    public GroupChannel(String id) {
        super(null);
        this.id = id;
    }

    public void save() {
        RoseChat.getInstance().getManager(GroupManager.class).createOrUpdateGroupChat(this);
    }

    @Override
    public void onLoad(String id, ConfigurationSection config) {
        // No implementation
    }

    @Override
    public void forceJoin(UUID uuid) {
        // No implementation
    }

    @Override
    public void kick(UUID uuid) {
        // No implementation
    }

    @Override
    public void send(RosePlayer sender, String message) {
        // Parses the first message synchronously
        // Allows for creating a token storage.
        RoseMessage roseMessage = RoseMessage.forLocation(sender, MessageLocation.GROUP);
        roseMessage.setPlayerInput(message);
        roseMessage.setPlaceholders(this.getInfoPlaceholders(sender, null, null, null).build());

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
            if (playerData != null && !this.canPlayerReceiveMessage(receiver, playerData, sender.getUUID())) continue;

            RoseChat.MESSAGE_THREAD_POOL.execute(() -> {
                receiver.send(roseMessage.parse(receiver, this.getFormat()).content());
            });
        }

        for (UUID uuid : RoseChatAPI.getInstance().getPlayerDataManager().getGroupSpies()) {
            if (this.members.contains(uuid)) continue;

            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;

            RosePlayer rosePlayer = new RosePlayer(player);
            PlayerData playerData = RoseChatAPI.getInstance().getPlayerData(uuid);

            // Don't send the message if the receiver can't receive it.
            if (!this.canPlayerReceiveMessage(rosePlayer, playerData, sender.getUUID())) continue;

            RoseChat.MESSAGE_THREAD_POOL.execute(() -> {
                rosePlayer.send(roseMessage.parse(rosePlayer, Setting.GROUP_SPY_FORMAT.getString()).content());
            });
        }
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
    public void onJoin(Player player) {
        // No implementation
    }

    @Override
    public void onLeave(Player player) {
        // No implementation
    }

    public void addMember(UUID uuid) {
        this.members.add(uuid);
    }

    public void removeMember(UUID uuid) {
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
    public boolean canJoinByCommand(Player player) {
        return Setting.CAN_JOIN_GROUP_CHANNELS.getBoolean();
    }

    @Override
    public String getFormat() {
        return Setting.GROUP_FORMAT.getString();
    }

    @Override
    public StringPlaceholders.Builder getInfoPlaceholders(RosePlayer sender, String trueValue, String falseValue, String nullValue) {
        return super.getInfoPlaceholders(sender, trueValue, falseValue, nullValue)
                .add("owner", Bukkit.getOfflinePlayer(this.owner).getName())
                .add("name", this.name)
                .add("group_id", this.getId())
                .add("group", this.getId())
                .add("group_owner", Bukkit.getOfflinePlayer(this.owner).getName())
                .add("group_name", this.name);
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

}

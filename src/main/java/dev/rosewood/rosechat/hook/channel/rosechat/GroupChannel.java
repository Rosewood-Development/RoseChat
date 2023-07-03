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
import net.md_5.bungee.api.chat.BaseComponent;
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
        // Create the rules for this message.
        MessageRules rules = new MessageRules().applyAllFilters().applySenderChatColor();

        // Parses the first message synchronously
        // Allows for creating a token storage.
        RoseMessage roseMessage = new RoseMessage(sender, MessageLocation.GROUP, message);
        roseMessage.setPlaceholders(this.getInfoPlaceholders(sender, null, null, null).build());
        roseMessage.applyRules(rules);

        // Check if the message is allowed to be sent.
        if (roseMessage.getOutputs().isBlocked()) {
            if (roseMessage.getOutputs().getFilterType() != null)
                roseMessage.getOutputs().getFilterType().sendWarning(sender);
            return;
        }

        BaseComponent[] parsedConsoleMessage = roseMessage.parse(new RosePlayer(Bukkit.getConsoleSender()), this.getFormat());

        // Send the parsed message to the console
        Bukkit.getConsoleSender().spigot().sendMessage(parsedConsoleMessage);

        for (UUID uuid : this.getMembers()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;

            // Clone the message for viewer-specific placeholders.
            RoseMessage playerMessage = new RoseMessage(roseMessage);
            RosePlayer rosePlayer = new RosePlayer(player);
            PlayerData playerData = RoseChatAPI.getInstance().getPlayerData(uuid);

            // Don't send the message if the receiver can't receive it.
            if (!this.canReceiveMessage(rosePlayer, playerData, sender.getUUID())) return;

            RoseChat.MESSAGE_THREAD_POOL.submit(() -> {
               rosePlayer.send(playerMessage.parse(rosePlayer, this.getFormat()));
            });
        }

        for (UUID uuid : RoseChatAPI.getInstance().getPlayerDataManager().getGroupSpies()) {
            if (this.members.contains(uuid)) continue;

            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;

            // Clone the message for viewer-specific placeholders.
            RoseMessage playerMessage = new RoseMessage(roseMessage);
            RosePlayer rosePlayer = new RosePlayer(player);
            PlayerData playerData = RoseChatAPI.getInstance().getPlayerData(uuid);

            // Don't send the message if the receiver can't receive it.
            if (!this.canReceiveMessage(rosePlayer, playerData, sender.getUUID())) return;

            RoseChat.MESSAGE_THREAD_POOL.submit(() -> {
                rosePlayer.send(playerMessage.parse(rosePlayer, Setting.GROUP_SPY_FORMAT.getString()));
            });
        }
    }

    @Override
    public void send(RoseMessage message, String discordId) {
        // No discord support for GroupChats
    }

    @Override
    public void send(RosePlayer sender, String message, UUID messageId) {
        // No bungee support for GroupChats
    }

    @Override
    public void sendJson(RosePlayer sender, String message, UUID messageId) {
        // No bungee support for GroupChats
    }

    @Override
    public boolean canReceiveMessage(RosePlayer receiver, PlayerData data, UUID senderUUID) {
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
                .addPlaceholder("owner", Bukkit.getOfflinePlayer(this.owner).getName())
                .addPlaceholder("name", this.name)
                .addPlaceholder("group_id", this.getId())
                .addPlaceholder("group", this.getId())
                .addPlaceholder("group_owner", Bukkit.getOfflinePlayer(this.owner).getName())
                .addPlaceholder("group_name", this.name);
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

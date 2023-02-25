package dev.rosewood.rosechat.hook.channel.rosechat;

import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.hook.channel.ChannelProvider;
import dev.rosewood.rosechat.message.RosePlayer;
import org.bukkit.entity.Player;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class GroupChannel extends Channel {

    private String name;
    private UUID owner;

    public GroupChannel(ChannelProvider provider) {
        super(provider);
    }

    @Override
    public void send(RosePlayer sender, String message) {

    }

    @Override
    public List<UUID> getMembers(RosePlayer sender) {
        return null;
    }

    @Override
    public String getId() {
        return "group";
    }

    @Override
    public List<String> getServers() {
        return Collections.emptyList();
    }

    @Override
    public boolean canJoinByCommand(Player player) {
        return true;
    }

    public String getName() {
        return this.name;
    }

    public UUID getOwner() {
        return this.owner;
    }

}

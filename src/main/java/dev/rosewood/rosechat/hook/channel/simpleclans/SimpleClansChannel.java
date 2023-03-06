package dev.rosewood.rosechat.hook.channel.simpleclans;

import dev.rosewood.rosechat.hook.channel.ChannelProvider;
import dev.rosewood.rosechat.hook.channel.rosechat.RoseChatChannel;
import dev.rosewood.rosechat.message.RosePlayer;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import java.util.List;

public class SimpleClansChannel extends RoseChatChannel {

    private SimpleClansChannelType channelType;

    public SimpleClansChannel(ChannelProvider provider) {
        super(provider);
    }

    @Override
    public void onLoad(String id, ConfigurationSection config) {
        super.onLoad(id, config);
        if (config.contains("channel-type")) this.channelType = SimpleClansChannelType.valueOf(config.getString("channel-type").toUpperCase());

    }

    @Override
    public List<Player> getVisibleAnywhereRecipients(RosePlayer sender, World world) {
        return super.getVisibleAnywhereRecipients(sender, world);
    }

    /*@Override
    public List<UUID> getMembers(RosePlayer sender) {
        List<UUID> members = new ArrayList<>();
        Clan clan = SimpleClans.getInstance().getClanManager().getClanByPlayerUniqueId(sender.getUUID());

        if (clan == null) return members;
        if (this.channelType == SimpleClansChannelType.ALLY) {
            clan.getMembers().forEach(member -> members.add(member.getUniqueId()));
        } else {
            clan.getAllAllyMembers().forEach(member -> members.add(member.getUniqueId()));
        }

        return new ArrayList<>(PartyAPI.getMembersMap(sender.asPlayer()).keySet());
    }*/

    public SimpleClansChannelType getChannelType() {
        return this.channelType;
    }

    public enum SimpleClansChannelType {

        CLAN,
        ALLY

    }

}

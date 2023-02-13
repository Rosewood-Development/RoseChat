package dev.rosewood.rosechat.hook.channel.simpleclans;

import com.gmail.nossr50.api.PartyAPI;
import dev.rosewood.rosechat.hook.channel.ChannelProvider;
import dev.rosewood.rosechat.hook.channel.rosechat.RoseChatChannel;
import dev.rosewood.rosechat.message.RosePlayer;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    public void send(RosePlayer sender, String message) {
        for (UUID uuid : this.getMembers(sender)) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;
            player.sendMessage("[Clan] " + message);
        }
    }

    @Override
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
    }

    public SimpleClansChannelType getChannelType() {
        return this.channelType;
    }

    public enum SimpleClansChannelType {

        CLAN,
        ALLY

    }

}

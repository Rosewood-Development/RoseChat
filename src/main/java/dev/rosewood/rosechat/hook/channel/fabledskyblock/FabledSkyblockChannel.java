package dev.rosewood.rosechat.hook.channel.fabledskyblock;

import dev.rosewood.rosechat.hook.channel.ChannelProvider;
import dev.rosewood.rosechat.hook.channel.rosechat.RoseChatChannel;
import dev.rosewood.rosechat.message.RosePlayer;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;

public class FabledSkyblockChannel extends RoseChatChannel {

    private FabledSkyblockChannelType channelType;

    public FabledSkyblockChannel(ChannelProvider provider) {
        super(provider);
    }

    @Override
    public void onLoad(String id, ConfigurationSection config) {
        super.onLoad(id, config);
        if (config.contains("channel-type")) this.channelType = FabledSkyblockChannelType.valueOf(config.getString("channel-type").toUpperCase());
    }

    @Override
    public List<Player> getVisibleAnywhereRecipients(RosePlayer sender, World world) {
        return super.getVisibleAnywhereRecipients(sender, world);
    }

    /*@Override
    public List<UUID> getMembers(RosePlayer sender) {
        Island island = SkyBlockAPI.getIslandManager().getIsland(sender.asPlayer());
        if (island == null) return new ArrayList<>();

        return this.channelType == FabledSkyblockChannelType.TEAM ? new ArrayList<>(island.getCoopPlayers().keySet())
                : SkyBlockAPI.getImplementation().getIslandManager().getPlayersAtIsland(island.getIsland()).stream().map(Player::getUniqueId).collect(Collectors.toList());

    }*/


    public FabledSkyblockChannelType getChannelType() {
        return channelType;
    }

    public enum FabledSkyblockChannelType {

        LOCAL,
        TEAM

    }

}

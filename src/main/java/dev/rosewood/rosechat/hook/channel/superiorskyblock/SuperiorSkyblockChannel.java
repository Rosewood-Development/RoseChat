package dev.rosewood.rosechat.hook.channel.superiorskyblock;

import dev.rosewood.rosechat.hook.channel.ChannelProvider;
import dev.rosewood.rosechat.hook.channel.rosechat.RoseChatChannel;
import dev.rosewood.rosechat.message.RosePlayer;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;

public class SuperiorSkyblockChannel extends RoseChatChannel {

    private SuperiorSkyblockChannelType channelType;

    public SuperiorSkyblockChannel(ChannelProvider provider) {
        super(provider);
    }

    @Override
    public void onLoad(String id, ConfigurationSection config) {
        super.onLoad(id, config);
        if (config.contains("channel-type")) this.channelType = SuperiorSkyblockChannelType.valueOf(config.getString("channel-type").toUpperCase());
    }

    @Override
    public List<Player> getVisibleAnywhereRecipients(RosePlayer sender, World world) {
        return super.getVisibleAnywhereRecipients(sender, world);
    }

    /*@Override
    public List<UUID> getMembers(RosePlayer sender) {
        Island island = SuperiorSkyblockAPI.getPlayer(sender.getUUID()).getIsland();
        if (island == null) return new ArrayList<>();

        return this.channelType == SuperiorSkyblockChannelType.TEAM ?
                island.getIslandMembers(true).stream().map(SuperiorPlayer::getUniqueId).collect(Collectors.toList()) :
                island.getAllPlayersInside().stream().map(SuperiorPlayer::getUniqueId).collect(Collectors.toList());

    }*/

    public SuperiorSkyblockChannelType getChannelType() {
        return channelType;
    }

    public enum SuperiorSkyblockChannelType {

        LOCAL,
        TEAM

    }

}

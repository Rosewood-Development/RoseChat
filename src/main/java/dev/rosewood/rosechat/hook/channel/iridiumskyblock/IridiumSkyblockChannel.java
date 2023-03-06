package dev.rosewood.rosechat.hook.channel.iridiumskyblock;

import dev.rosewood.rosechat.hook.channel.ChannelProvider;
import dev.rosewood.rosechat.hook.channel.rosechat.RoseChatChannel;
import dev.rosewood.rosechat.message.RosePlayer;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import java.util.List;

public class IridiumSkyblockChannel extends RoseChatChannel {

    private IridiumSkyblockChannelType channelType;

    public IridiumSkyblockChannel(ChannelProvider provider) {
        super(provider);
    }

    @Override
    public void onLoad(String id, ConfigurationSection config) {
        super.onLoad(id, config);
        if (config.contains("channel-type")) this.channelType = IridiumSkyblockChannelType.valueOf(config.getString("channel-type").toUpperCase());
    }

    @Override
    public List<Player> getVisibleAnywhereRecipients(RosePlayer sender, World world) {
        return super.getVisibleAnywhereRecipients(sender, world);
    }

    /*@Override
    public List<UUID> getMembers(RosePlayer sender) {
        User user = IridiumSkyblockAPI.getInstance().getUser(sender.asPlayer());
        if (!user.getIsland().isPresent()) return new ArrayList<>();

        Island island = user.getIsland().get();
        return this.channelType == IridiumSkyblockChannelType.TEAM ?
                island.getMembers().stream().map(User::getUuid).collect(Collectors.toList()) :
                IridiumSkyblock.getInstance().getIslandManager().getPlayersOnIsland(island).stream().map(User::getUuid).collect(Collectors.toList());
    }*/

    public IridiumSkyblockChannelType getChannelType() {
        return channelType;
    }

    public enum IridiumSkyblockChannelType {

        LOCAL,
        TEAM

    }

}

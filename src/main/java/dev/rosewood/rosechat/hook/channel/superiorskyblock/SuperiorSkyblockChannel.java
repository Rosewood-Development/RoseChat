package dev.rosewood.rosechat.hook.channel.superiorskyblock;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import dev.rosewood.rosechat.hook.channel.ChannelProvider;
import dev.rosewood.rosechat.hook.channel.rosechat.RoseChatChannel;
import dev.rosewood.rosechat.message.RosePlayer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
    public void send(RosePlayer sender, String message) {
        for (UUID uuid : this.getMembers(sender)) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;
            player.sendMessage("[Team] " + message);
        }
    }

    @Override
    public List<UUID> getMembers(RosePlayer sender) {
        Island island = SuperiorSkyblockAPI.getPlayer(sender.getUUID()).getIsland();
        if (island == null) return new ArrayList<>();

        return this.channelType == SuperiorSkyblockChannelType.TEAM ?
                island.getIslandMembers(true).stream().map(SuperiorPlayer::getUniqueId).collect(Collectors.toList()) :
                island.getAllPlayersInside().stream().map(SuperiorPlayer::getUniqueId).collect(Collectors.toList());

    }

    // Temporary copy for any other settings that need to be added later
    public enum SuperiorSkyblockChannelType {

        LOCAL,
        TEAM

    }

}

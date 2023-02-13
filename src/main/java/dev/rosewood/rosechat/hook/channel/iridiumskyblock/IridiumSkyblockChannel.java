package dev.rosewood.rosechat.hook.channel.iridiumskyblock;

import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.api.IridiumSkyblockAPI;
import com.iridium.iridiumskyblock.database.Island;
import com.iridium.iridiumskyblock.database.User;
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
    public void send(RosePlayer sender, String message) {
        for (UUID uuid : this.getMembers(sender)) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;
            player.sendMessage("[Team] " + message);
        }
    }

    @Override
    public List<UUID> getMembers(RosePlayer sender) {
        User user = IridiumSkyblockAPI.getInstance().getUser(sender.asPlayer());
        if (!user.getIsland().isPresent()) return new ArrayList<>();

        Island island = user.getIsland().get();
        return this.channelType == IridiumSkyblockChannelType.TEAM ?
                island.getMembers().stream().map(User::getUuid).collect(Collectors.toList()) :
                IridiumSkyblock.getInstance().getIslandManager().getPlayersOnIsland(island).stream().map(User::getUuid).collect(Collectors.toList());
    }

    // Temporary copy for any other settings that need to be added later
    public enum IridiumSkyblockChannelType {

        LOCAL,
        TEAM

    }

}

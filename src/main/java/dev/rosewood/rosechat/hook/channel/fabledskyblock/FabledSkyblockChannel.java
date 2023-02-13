package dev.rosewood.rosechat.hook.channel.fabledskyblock;

import com.songoda.skyblock.api.SkyBlockAPI;
import com.songoda.skyblock.api.island.Island;
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
    public void send(RosePlayer sender, String message) {
        for (UUID uuid : this.getMembers(sender)) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;
            player.sendMessage("[Team] " + message);
        }
    }

    @Override
    public List<UUID> getMembers(RosePlayer sender) {
        Island island = SkyBlockAPI.getIslandManager().getIsland(sender.asPlayer());
        if (island == null) return new ArrayList<>();

        return this.channelType == FabledSkyblockChannelType.TEAM ? new ArrayList<>(island.getCoopPlayers().keySet())
                : SkyBlockAPI.getImplementation().getIslandManager().getPlayersAtIsland(island.getIsland()).stream().map(Player::getUniqueId).collect(Collectors.toList());

    }

    // Temporary copy for any other settings that need to be added later
    public enum FabledSkyblockChannelType {

        LOCAL,
        TEAM

    }

}

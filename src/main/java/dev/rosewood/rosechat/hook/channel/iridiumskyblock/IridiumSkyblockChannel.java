package dev.rosewood.rosechat.hook.channel.iridiumskyblock;

import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.api.IridiumSkyblockAPI;
import com.iridium.iridiumskyblock.database.Island;
import com.iridium.iridiumskyblock.database.User;
import dev.rosewood.rosechat.hook.channel.ChannelProvider;
import dev.rosewood.rosechat.hook.channel.rosechat.RoseChatChannel;
import dev.rosewood.rosechat.message.RosePlayer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import java.util.ArrayList;
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
        if (!config.contains("visible-anywhere")) this.visibleAnywhere = true;
    }

    @Override
    public List<Player> getVisibleAnywhereRecipients(RosePlayer sender, World world) {
        List<Player> recipients = new ArrayList<>();

        if (!sender.isPlayer()) return recipients;
        User user = IridiumSkyblockAPI.getInstance().getUser(sender.asPlayer());
        if (!user.getIsland().isPresent()) return recipients;

        Island island = user.getIsland().isPresent() ? user.getIsland().get() : null;
        if (island == null) return recipients;

        if (this.channelType == IridiumSkyblockChannelType.TEAM) {
            for (User member : island.getMembers()) {
                if (member == null) continue;

                Player player = Bukkit.getPlayer(member.getUuid());
                if (player == null) continue;
                recipients.add(player);
            }
        } else {
            for (User member : IridiumSkyblock.getInstance().getIslandManager().getPlayersOnIsland(island)) {
                if (member == null) continue;

                Player player = Bukkit.getPlayer(member.getUuid());
                if (player == null) continue;
                recipients.add(player);
            }
        }

        return recipients;
    }

    public IridiumSkyblockChannelType getChannelType() {
        return channelType;
    }

    public enum IridiumSkyblockChannelType {

        LOCAL,
        TEAM

    }

}

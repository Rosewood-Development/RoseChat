package dev.rosewood.rosechat.hook.channel.superiorskyblock;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import dev.rosewood.rosechat.hook.channel.ChannelProvider;
import dev.rosewood.rosechat.hook.channel.rosechat.RoseChatChannel;
import dev.rosewood.rosechat.message.RosePlayer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import java.util.ArrayList;
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
        if (!config.contains("visible-anywhere")) this.visibleAnywhere = true;
    }

    @Override
    public List<Player> getVisibleAnywhereRecipients(RosePlayer sender, World world) {
        List<Player> recipients = new ArrayList<>();

        if (!sender.isPlayer()) return recipients;
        Island island = SuperiorSkyblockAPI.getPlayer(sender.getUUID()).getIsland();
        if (island == null) return recipients;

        if (this.channelType == SuperiorSkyblockChannelType.TEAM) {
            for (SuperiorPlayer sPlayer : island.getIslandMembers(true)) {
                if (sPlayer == null || !sPlayer.isOnline()) continue;
                Player player = Bukkit.getPlayer(sPlayer.getUniqueId());
                if (player == null) continue;
                recipients.add(player);
            }
        } else {
            for (SuperiorPlayer sPlayer : island.getAllPlayersInside()) {
                if (sPlayer == null || !sPlayer.isOnline()) continue;
                Player player = Bukkit.getPlayer(sPlayer.getUniqueId());
                if (player == null) continue;
                recipients.add(player);
            }
        }

        return recipients;
    }

    public SuperiorSkyblockChannelType getChannelType() {
        return channelType;
    }

    public enum SuperiorSkyblockChannelType {

        LOCAL,
        TEAM

    }

}

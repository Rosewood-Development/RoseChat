package dev.rosewood.rosechat.hook.channel.fabledskyblock;

import com.songoda.skyblock.api.SkyBlockAPI;
import com.songoda.skyblock.api.island.Island;
import com.songoda.skyblock.api.island.IslandRole;
import dev.rosewood.rosechat.hook.channel.ChannelProvider;
import dev.rosewood.rosechat.hook.channel.rosechat.RoseChatChannel;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FabledSkyblockChannel extends RoseChatChannel {

    private FabledSkyblockChannelType channelType;

    public FabledSkyblockChannel(ChannelProvider provider) {
        super(provider);
    }

    @Override
    public void onLoad(String id, ConfigurationSection config) {
        super.onLoad(id, config);

        if (config.contains("channel-type")) this.channelType = FabledSkyblockChannelType.valueOf(config.getString("channel-type").toUpperCase());
        if (!config.contains("visible-anywhere")) this.visibleAnywhere = true;
    }

    @Override
    public List<Player> getVisibleAnywhereRecipients(RosePlayer sender, World world) {
        List<Player> recipients = new ArrayList<>();

        if (!sender.isPlayer()) return recipients;
        Island island = SkyBlockAPI.getIslandManager().getIsland(sender.asPlayer());
        if (island == null) return recipients;

        if (this.channelType == FabledSkyblockChannelType.TEAM) {
            for (UUID uuid : island.getPlayersWithRole(IslandRole.MEMBER)) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null && this.getReceiveCondition(sender, player)) recipients.add(player);
            }
        } else if (this.channelType == FabledSkyblockChannelType.LOCAL) {
            for (Player player : SkyBlockAPI.getIslandManager().getPlayersAtIsland(island)) {
                if (player != null && this.getReceiveCondition(sender, player)) recipients.add(player);
            }
        } else {
            for (UUID uuid : island.getCoopPlayers().keySet()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null && this.getReceiveCondition(sender, player)) recipients.add(player);
            }
        }

        return recipients;
    }

    @Override
    public boolean canJoinByCommand(Player player) {
        Island island = SkyBlockAPI.getIslandManager().getIsland(player);
        if (island == null) return false;

        return super.canJoinByCommand(player);
    }

    @Override
    public StringPlaceholders.Builder getInfoPlaceholders(RosePlayer sender, String trueValue, String falseValue, String nullValue) {
        return super.getInfoPlaceholders(sender, trueValue, falseValue, nullValue)
                .addPlaceholder("type", this.channelType.toString().toLowerCase());
    }

    public FabledSkyblockChannelType getChannelType() {
        return channelType;
    }

    public enum FabledSkyblockChannelType {

        LOCAL,
        TEAM

    }

}

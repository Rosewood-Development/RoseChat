package dev.rosewood.rosechat.hook.channel.bentobox;

import dev.rosewood.rosechat.hook.channel.ChannelProvider;
import dev.rosewood.rosechat.hook.channel.rosechat.RoseChatChannel;
import dev.rosewood.rosechat.message.RosePlayer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BentoBoxChannel extends RoseChatChannel {

    private BentoBoxChannelType channelType;

    public BentoBoxChannel(ChannelProvider provider) {
        super(provider);
    }

    @Override
    public void onLoad(String id, ConfigurationSection config) {
        super.onLoad(id, config);
        if (config.contains("channel-type")) this.channelType = BentoBoxChannelType.valueOf(config.getString("channel-type").toUpperCase());
        if (!config.contains("visible-anywhere")) this.visibleAnywhere = true;
    }

    @Override
    public List<Player> getVisibleAnywhereRecipients(RosePlayer sender, World world) {
        List<Player> recipients = new ArrayList<>();

        if (!sender.isPlayer()) return recipients;
        Island island = BentoBox.getInstance().getIslandsManager().getIsland(world, sender.getUUID());
        if (island == null) return recipients;

        if (this.channelType == BentoBoxChannelType.TEAM) {
            for (UUID uuid : island.getMemberSet()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null) continue;
                recipients.add(player);
            }
        } else {
            recipients.addAll(island.getPlayersOnIsland());
        }

        return recipients;
    }

    public BentoBoxChannelType getChannelType() {
        return channelType;
    }

    public enum BentoBoxChannelType {

        LOCAL,
        TEAM

    }

}

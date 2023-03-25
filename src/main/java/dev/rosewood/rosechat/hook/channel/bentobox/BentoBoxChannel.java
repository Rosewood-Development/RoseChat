package dev.rosewood.rosechat.hook.channel.bentobox;

import dev.rosewood.rosechat.hook.channel.ChannelProvider;
import dev.rosewood.rosechat.hook.channel.rosechat.RoseChatChannel;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.RanksManager;

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
        Island island = BentoBox.getInstance().getIslandsManager().getIsland(sender.asPlayer().getWorld(), sender.getUUID());
        if (island == null) return recipients;

        if (this.channelType == BentoBoxChannelType.TEAM) {
            for (UUID uuid : island.getMemberSet()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null && this.getReceiveCondition(sender, player)) recipients.add(player);
            }
        } else if (this.channelType == BentoBoxChannelType.LOCAL) {
            for (Player player : island.getPlayersOnIsland()) {
                if (player != null && this.getReceiveCondition(sender, player)) recipients.add(player);
            }
        } else if (this.channelType == BentoBoxChannelType.COOP) {
            for (UUID uuid : island.getMemberSet(RanksManager.COOP_RANK)) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null && this.getReceiveCondition(sender, player)) recipients.add(player);
            }
        }

        return recipients;
    }

    @Override
    public boolean canJoinByCommand(Player player) {
        Island island = BentoBox.getInstance().getIslandsManager().getIsland(player.getWorld(), player.getUniqueId());
        if (island == null) return false;

        return super.canJoinByCommand(player);
    }

    @Override
    public StringPlaceholders.Builder getInfoPlaceholders(RosePlayer sender, String trueValue, String falseValue, String nullValue) {
        return super.getInfoPlaceholders(sender, trueValue, falseValue, nullValue)
                .addPlaceholder("type", this.channelType.toString().toLowerCase());
    }

    public BentoBoxChannelType getChannelType() {
        return channelType;
    }

    public enum BentoBoxChannelType {

        LOCAL,
        TEAM,
        COOP

    }

}

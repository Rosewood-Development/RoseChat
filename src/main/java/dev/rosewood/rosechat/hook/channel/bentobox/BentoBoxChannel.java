package dev.rosewood.rosechat.hook.channel.bentobox;

import dev.rosewood.rosechat.hook.channel.ChannelProvider;
import dev.rosewood.rosechat.hook.channel.rosechat.RoseChatChannel;
import dev.rosewood.rosechat.message.RosePlayer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class BentoBoxChannel extends RoseChatChannel {

    private BentoBoxChannelType channelType;

    public BentoBoxChannel(ChannelProvider provider) {
        super(provider);
    }

    @Override
    public void onLoad(String id, ConfigurationSection config) {
        super.onLoad(id, config);
        if (config.contains("channel-type")) this.channelType = BentoBoxChannelType.valueOf(config.getString("channel-type").toUpperCase());
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
        World world = sender.asPlayer().getWorld();
        Island island = BentoBox.getInstance().getIslandsManager().getIsland(world, sender.getUUID());

        if (island == null) return new ArrayList<>();

        return this.channelType == BentoBoxChannelType.TEAM ?
                new ArrayList<>(island.getMemberSet()) :
                island.getPlayersOnIsland().stream().map(Entity::getUniqueId).collect(Collectors.toList());
    }

    public enum BentoBoxChannelType {

        LOCAL,
        TEAM

    }

}

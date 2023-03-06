package dev.rosewood.rosechat.hook.channel.factionsuuid;

import dev.rosewood.rosechat.hook.channel.ChannelProvider;
import dev.rosewood.rosechat.hook.channel.rosechat.RoseChatChannel;
import dev.rosewood.rosechat.message.RosePlayer;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import java.util.List;

public class FactionsUUIDChannel extends RoseChatChannel {

    public FactionsUUIDChannel(ChannelProvider provider) {
        super(provider);
    }

    @Override
    public void onLoad(String id, ConfigurationSection config) {
        super.onLoad(id, config);
    }

    @Override
    public List<Player> getVisibleAnywhereRecipients(RosePlayer sender, World world) {
        return super.getVisibleAnywhereRecipients(sender, world);
    }

    /*@Override
    public List<UUID> getMembers(RosePlayer sender) {
        List<UUID> members = new ArrayList<>();
        Faction faction = FPlayers.getInstance().getByPlayer(sender.asPlayer()).getFaction();
        faction.getFPlayers().forEach(fplayer -> members.add(fplayer.getPlayer().getUniqueId()));
        return members;
    }*/

}

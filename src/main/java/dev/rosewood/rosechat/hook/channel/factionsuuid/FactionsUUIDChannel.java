package dev.rosewood.rosechat.hook.channel.factionsuuid;

import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import dev.rosewood.rosechat.hook.channel.ChannelProvider;
import dev.rosewood.rosechat.hook.channel.rosechat.RoseChatChannel;
import dev.rosewood.rosechat.message.RosePlayer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FactionsUUIDChannel extends RoseChatChannel {

    public FactionsUUIDChannel(ChannelProvider provider) {
        super(provider);
    }

    @Override
    public void onLoad(String id, ConfigurationSection config) {
        super.onLoad(id, config);
    }

    @Override
    public void send(RosePlayer sender, String message) {
        for (UUID uuid : this.getMembers(sender)) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;
            player.sendMessage("[Faction] " + message);
        }
    }

    @Override
    public List<UUID> getMembers(RosePlayer sender) {
        List<UUID> members = new ArrayList<>();
        Faction faction = FPlayers.getInstance().getByPlayer(sender.asPlayer()).getFaction();
        faction.getFPlayers().forEach(fplayer -> members.add(fplayer.getPlayer().getUniqueId()));
        return members;
    }

}

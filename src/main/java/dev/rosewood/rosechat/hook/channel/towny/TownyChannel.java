package dev.rosewood.rosechat.hook.channel.towny;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import dev.rosewood.rosechat.hook.channel.ChannelProvider;
import dev.rosewood.rosechat.hook.channel.rosechat.RoseChatChannel;
import dev.rosewood.rosechat.message.RosePlayer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TownyChannel extends RoseChatChannel {

    private TownyChannelType channelType;

    public TownyChannel(ChannelProvider provider) {
        super(provider);
    }

    @Override
    public void onLoad(String id, ConfigurationSection config) {
        super.onLoad(id, config);
        if (config.contains("towny-channel-type")) this.channelType = TownyChannelType.valueOf(config.getString("towny-channel-type"));
    }

    @Override
    public void send(RosePlayer sender, String message) {
        for (UUID uuid : this.getMembers(sender)) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;
            player.sendMessage("[towny]" + message);
        }
    }

    @Override
    public List<UUID> getMembers(RosePlayer sender) {
        if (this.channelType == TownyChannelType.TOWN) {
            Town town = TownyAPI.getInstance().getTown(sender.asPlayer());
            if (town == null) return Collections.emptyList();

            return town.getResidents().stream().map(Resident::getUUID).collect(Collectors.toList());
        } else {
            Nation nation = TownyAPI.getInstance().getNation(sender.asPlayer());
            if (nation == null) return Collections.emptyList();

            return nation.getResidents().stream().map(Resident::getUUID).collect(Collectors.toList());
        }
    }

    @Override
    public String getId() {
        return "towny";
    }

    public enum TownyChannelType {

        TOWN,
        NATION

    }

}

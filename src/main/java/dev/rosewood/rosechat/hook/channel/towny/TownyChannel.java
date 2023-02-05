package dev.rosewood.rosechat.hook.channel.towny;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import dev.rosewood.rosechat.hook.channel.ChannelProvider;
import dev.rosewood.rosechat.hook.channel.rosechat.RoseChatChannel;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TownyChannel extends RoseChatChannel {

    public TownyChannel(ChannelProvider provider) {
        super(provider);
    }

    @Override
    public void onLoad(String id, ConfigurationSection config) {
        super.onLoad(id, config);
    }

    @Override
    public void send(MessageWrapper message) {

    }

    @Override
    public List<UUID> getMembers(RoseSender sender) {
        Town town = TownyAPI.getInstance().getTown(sender.getUUID());
        if (town == null) return Collections.emptyList();

        return town.getResidents().stream().map(Resident::getUUID).collect(Collectors.toList());
    }

    @Override
    public String getId() {
        return "towny";
    }

}
